package top.cjf_rb.core.pojo.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import top.cjf_rb.core.constant.AppSystemConst;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 分页响应

 @author cjf */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
public class PagingVo<E> implements Serializable {
    @Serial
    private static final long serialVersionUID = AppSystemConst.SERIAL_VERSION_UID;

    /**
     列表数据
     */
    private List<E> records;

    /**
     页数
     */
    @NotNull
    private Long page;

    /**
     分页大小
     */
    @NotNull
    private Long size;

    /**
     数据总数
     */
    @NotNull
    private Long total;

    public static <T> PagingVo<T> of(Long page, Long size, Long total, List<T> records) {
        return new PagingVo<T>().setPage(page)
                                .setSize(size)
                                .setTotal(total)
                                .setRecords(records);
    }

    /**
     空分页
     */
    public static <T> PagingVo<T> empty() {
        return of(0L, 0L, 0L, Collections.emptyList());
    }

}
