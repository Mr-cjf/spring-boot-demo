package top.cjf_rb.xxl_job.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 @author lty
 @since 1.0 */
@Data
@Accessors(chain = true)
public class JobGroupQuery {

    private String appName;

    private String title;

    private Integer start = 0;

    private Integer length = 10;

}
