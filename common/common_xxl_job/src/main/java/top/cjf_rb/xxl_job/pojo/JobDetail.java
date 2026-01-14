package top.cjf_rb.xxl_job.pojo;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import top.cjf_rb.core.constant.AppSystemConst;
import top.cjf_rb.core.constant.SeparatorEnum;
import top.cjf_rb.xxl_job.constant.*;

import java.io.Serial;
import java.io.Serializable;

/**
 xxl job

 @author cjf */
@Data
@Accessors(chain = true)
public class JobDetail implements Serializable {
    @Serial
    private static final long serialVersionUID = AppSystemConst.SERIAL_VERSION_UID;
    /**
     调度类型: {@link ScheduleType}
     */
    @NotBlank
    private String scheduleType = ScheduleType.CRON;
    /**
     调度过期策略: {@link MisfireStrategy}
     */
    @NotBlank
    private String misfireStrategy = MisfireStrategy.DO_NOTHING;
    /**
     执行器路由策略: {@link ExecutorRouteStrategy}
     */
    @NotBlank
    private String executorRouteStrategy = ExecutorRouteStrategy.FIRST;
    /**
     阻塞处理策略: {@link ExecutorBlockStrategy}
     */
    @NotBlank
    private String executorBlockStrategy = ExecutorBlockStrategy.SERIAL_EXECUTION;
    /**
     子任务ID，多个逗号分隔
     */
    private String childJobId = SeparatorEnum.NO.getSeparator();
    /**
     运行模式: {@link GlueType}
     */
    @NotBlank
    private String glueType = GlueType.BEAN;
    private String glueSource = SeparatorEnum.NO.getSeparator();
    private String glueRemark = "GLUE代码初始化";
    /**
     执行器主键ID
     */
    private int jobGroup;
    @NotBlank
    private String jobDesc;
    /**
     负责人
     */
    @NotBlank
    @Size(max = 20)
    private String author;
    /**
     报警邮件
     */
    private String alarmEmail;
    /**
     调度配置，值含义取决于调度类型
     */
    @NotNull
    private String scheduleConf;
    /**
     执行器，任务Handler名称
     */
    @NotBlank
    private String executorHandler;
    /**
     执行器，任务参数
     */
    private String executorParam;
    /**
     任务执行超时时间，单位秒
     */
    private int executorTimeout;
    /**
     失败重试次数
     */
    private int executorFailRetryCount;

}
