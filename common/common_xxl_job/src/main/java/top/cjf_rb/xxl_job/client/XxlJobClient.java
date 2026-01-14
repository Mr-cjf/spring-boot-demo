package top.cjf_rb.xxl_job.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;
import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.exception.AppException;
import top.cjf_rb.xxl_job.config.XxlJobProperties;
import top.cjf_rb.xxl_job.pojo.JobDetail;
import top.cjf_rb.xxl_job.pojo.JobGroup;
import top.cjf_rb.xxl_job.pojo.JobGroupQuery;
import top.cjf_rb.xxl_job.pojo.JobPageDto;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 @author lty
 @since 1.0 */
@Slf4j
@Component
public class XxlJobClient {

    private static final String GROUP_SAVE = "/jobgroup/save";
    private static final String GROUP_LIST = "/jobgroup/pageList";
    private static final String JOB_ADD = "/jobinfo/add";
    private static final String JOB_START = "/jobinfo/start";
    private static final String JOB_STOP = "/jobinfo/stop";
    private static final String JOB_REMOVE = "/jobinfo/remove";

    @Resource
    @Qualifier("xxlJobRestTemplate")
    private RestTemplate xxlJobRestTemplate;
    @Resource
    private XxlJobProperties xxlJobProperties;
    @Resource
    private ObjectMapper objectMapper;

    /**
     获取身份信息, 格式: XXL_JOB_LOGIN_IDENTITY=xxxxxxx
     */
    private String getLoginIdentityKey() {
        HashMap<String, String> identityInfo = new HashMap<>(4);
        identityInfo.put("username", xxlJobProperties.getAdminUsername());
        identityInfo.put("password", xxlJobProperties.getAdminPassword());

        String tokenHex;
        try {
            String tokenJson = objectMapper.writeValueAsString(identityInfo);
            tokenHex = new BigInteger(tokenJson.getBytes()).toString(16);
        } catch (JsonProcessingException e) {
            throw new AppException(ErrorCodeEnum.UNKNOWN_ERROR, ">> xxl-job序列化异常", e);
        }

        return "XXL_JOB_LOGIN_IDENTITY=" + tokenHex;
    }

    /**
     获取xxl-job需要的请求头信息
     */
    private HttpHeaders xxlJobHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String loginIdentityKey = this.getLoginIdentityKey();
        headers.add(HttpHeaders.COOKIE, loginIdentityKey);

        return headers;
    }

    /**
     初始化执行器
     */
    public void initJobGroup() {
        XxlJobProperties.Executor executor = xxlJobProperties.getExecutor();
        final String appName = executor.getAppName();

        JobGroupQuery query = new JobGroupQuery().setAppName(appName);
        JobPageDto<JobGroup> jobPageDto = this.findJobGroup(query);
        if (jobPageDto.getRecordsTotal() > 0) {
            return;
        }

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>(4);
        params.add("appname", appName);
        params.add("title", "自动创建");
        params.add("addressType", 0);

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, this.xxlJobHeaders());
        String xxlUrl = xxlJobProperties.getAdminAddresses() + GROUP_SAVE;

        xxlJobRestTemplate.postForEntity(xxlUrl, entity, Void.class);
    }

    /**
     查询执行器

     @param query {@link JobGroupQuery}
     */
    public JobPageDto<JobGroup> findJobGroup(JobGroupQuery query) {
        // 查询对应groupId:
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>(4);
        params.add("appname", query.getAppName());
        params.add("title", query.getTitle());
        params.add("start", query.getStart());
        params.add("length", query.getLength());

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, this.xxlJobHeaders());
        ParameterizedTypeReference<JobPageDto<JobGroup>> typeRef = new ParameterizedTypeReference<>() {
        };
        String xxlUrl = xxlJobProperties.getAdminAddresses() + GROUP_LIST;

        ResponseEntity<JobPageDto<JobGroup>> response = xxlJobRestTemplate.exchange(xxlUrl, HttpMethod.POST, entity,
                                                                                    typeRef);

        return response.getBody();
    }

    /**
     获取执行器ID(JobGroupId)
     */
    public Integer getGroupId() {
        XxlJobProperties.Executor executor = xxlJobProperties.getExecutor();
        final String appName = executor.getAppName();

        JobGroupQuery query = new JobGroupQuery().setAppName(appName);
        JobPageDto<JobGroup> body = findJobGroup(query);
        if (Objects.isNull(body) || body.getRecordsTotal() == 0) {
            String message = MessageFormat.format("xxl-job中查询不到相关任务执行器! 响应结果: {0}", body);
            throw new AppException(ErrorCodeEnum.UNKNOWN_ERROR, message);
        }

        // appName完全匹配才算正确
        Optional<JobGroup> jobExecutor = body.getData()
                                             .stream()
                                             .filter(e -> e.getAppName()
                                                           .equals(appName))
                                             .findFirst();
        if (jobExecutor.isEmpty()) {
            String message = MessageFormat.format("xxl-job中查询不到相关任务执行器! 响应结果: {0}", body);
            throw new AppException(ErrorCodeEnum.UNKNOWN_ERROR, message);
        }

        return jobExecutor.get()
                          .getId();
    }

    public Integer create(@NonNull @Validated JobDetail jobDetail) {
        jobDetail.setJobGroup(this.getGroupId());

        // params
        TypeReference<Map<String, Object>> ref = new TypeReference<>() {
        };
        Map<String, Object> readValue = objectMapper.convertValue(jobDetail, ref);

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.setAll(readValue);

        Map<String, Object> body = this.callXxlJob(params, JOB_ADD);
        // 从响应中提取ID，通常是"content"或"data"字段
        Object content = body.get("content");
        if (content == null) {
            content = body.get("data");
        }
        if (content != null) {
            return Integer.valueOf(content.toString());
        }
        throw new AppException(ErrorCodeEnum.UNKNOWN_ERROR, "创建JOB失败，未获取到JobId");
    }

    /**
     创建并启动job

     @param jobDetail {@link JobDetail}
     @return JobId
     */
    public Integer createAndStart(@NonNull @Validated JobDetail jobDetail) {
        // 创建任务
        Integer jobId = this.create(jobDetail);
        // 启动任务
        this.start(jobId);

        return jobId;
    }

    /**
     启动任务
     */
    public void start(int jobId) {
        this.executeStartOrStopOrRemove(jobId, JOB_START);
    }

    /**
     停止任务
     */
    public void stop(int jobId) {
        this.executeStartOrStopOrRemove(jobId, JOB_STOP);
    }

    /**
     删除job
     */
    public void remove(int jobId) {
        this.executeStartOrStopOrRemove(jobId, JOB_REMOVE);
    }

    /**
     执行 启动/停止/删除 Job

     @param jobId 任务ID
     @param uri   uri地址
     */
    private void executeStartOrStopOrRemove(int jobId, String uri) {
        // params
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>(1);
        params.add("id", jobId);

        this.callXxlJob(params, uri);
    }

    private Map<String, Object> callXxlJob(MultiValueMap<String, Object> params, String uri) {
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, this.xxlJobHeaders());
        ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<>() {
        };
        String xxlUrl = xxlJobProperties.getAdminAddresses() + uri;

        ResponseEntity<Map<String, Object>> response = xxlJobRestTemplate.exchange(xxlUrl, HttpMethod.POST, entity,
                                                                                   typeRef);

        Map<String, Object> body = response.getBody();
        if (Objects.isNull(body)) {
            String message = MessageFormat.format("执行xxl-job中[{0}]的定时任务失败! 请求参数:{1}, 响应结果为空", uri,
                                                  params);
            throw new AppException(ErrorCodeEnum.UNKNOWN_ERROR, message);
        }

        // 检查响应状态码，通常XXL-Job的响应中code字段表示执行结果
        Object codeObj = body.get("code");
        if (codeObj != null && !Integer.toString(HttpStatus.OK.value())
                                       .equals(codeObj.toString())) {
            String message = MessageFormat.format("执行xxl-job中[{0}]的定时任务失败! 请求参数:{1}, 响应结果: {2}", uri,
                                                  params, body);
            throw new AppException(ErrorCodeEnum.UNKNOWN_ERROR, message);
        }

        return body;
    }

}
