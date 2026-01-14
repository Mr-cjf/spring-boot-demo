package top.cjf_rb.mp.pojo.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.cjf_rb.core.pojo.vo.PagingVo;

import java.util.Objects;

/**
 @author cjf
 @since 1.0 */
public class MpPagingVo<E> extends PagingVo<E> {

    public static <T> PagingVo<T> of(IPage<T> page) {
        if (Objects.isNull(page)) {
            return MpPagingVo.empty();
        }

        return new PagingVo<T>().setPage(page.getPages())
                                .setSize(page.getSize())
                                .setTotal(page.getTotal())
                                .setRecords(page.getRecords());
    }

}
