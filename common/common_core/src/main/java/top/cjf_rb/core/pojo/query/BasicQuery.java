package top.cjf_rb.core.pojo.query;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import top.cjf_rb.core.constant.AppSystemConst;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 基础的查询条件实体

 @author cjf */
@Data
@Accessors(chain = true)
public class BasicQuery implements Serializable {
    @Serial
    private static final long serialVersionUID = AppSystemConst.SERIAL_VERSION_UID;

    /**
     模糊搜索字段
     */
    private String filter;
    /**
     开始时间
     */
    private Instant startsAt;
    /**
     结束时间
     */
    private Instant endsAt;
    /**
     排序字段
     */
    private List<@NotBlank String> sorts;

    /**
     获取合法的排序字段
     */
    public List<String> obtainLegalSortFields() {
        return Collections.emptyList();
    }
}
