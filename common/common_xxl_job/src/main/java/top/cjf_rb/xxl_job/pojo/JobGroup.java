package top.cjf_rb.xxl_job.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 @author cjf
 @since 1.0 */
@Data
@Accessors(chain = true)
public class JobGroup {

    private Integer id;

    /**
     AppName
     */
    @JsonProperty("appname")
    private String appName;
    /**
     名称
     */
    private String title;

    /**
     执行器地址类型：0=自动注册、1=手动录入
     */
    private Integer addressType;
    /**
     执行器地址列表，多地址逗号分隔(手动录入)
     */
    private String addressList;

    private String updateTime;

    /**
     执行器地址列表(系统注册)
     */
    private List<String> registryList;

}
