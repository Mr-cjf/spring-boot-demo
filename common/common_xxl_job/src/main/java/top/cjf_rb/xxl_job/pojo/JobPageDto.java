package top.cjf_rb.xxl_job.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 xxl job分页模型

 @author cjf
 @since 1.0 */
@Data
@Accessors(chain = true)
public class JobPageDto<T> {

    private Integer recordsTotal;

    private Integer recordsFiltered;

    private List<T> data;

}
