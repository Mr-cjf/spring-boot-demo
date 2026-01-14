package top.cjf_rb.core.pojo.query;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import top.cjf_rb.core.web.validation.constraints.Rows;

/**
 分页查询实体

 @author cjf */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PagingQuery extends BasicQuery {

    /**
     哪页
     */
    @Min(1)
    private long page;
    /**
     几行
     */
    @Rows
    private long size;

}
