package top.cjf_rb.mp.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.lang.NonNull;
import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.pojo.query.BasicQuery;
import top.cjf_rb.core.pojo.query.PagingQuery;
import top.cjf_rb.core.pojo.vo.PagingVo;
import top.cjf_rb.core.util.Asserts;
import top.cjf_rb.mp.constant.BuiltInColumnConst;
import top.cjf_rb.mp.pojo.entity.BasicEntity;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 @author cjf
 @since 1.0 */
public class AppBasicService<M extends BaseMapper<E>, E extends BasicEntity<?>, Q extends PagingQuery> extends ServiceImpl<M, E> {

    private static final Pattern sortFieldPattern = Pattern.compile("^(\\w+)(:(asc|desc)$)?");

    /**
     条件分页查询

     @param query 查询参数
     @return 分页数据
     */
    public PagingVo<E> paging(@NonNull Q query) {
        // 分页
        Page<E> page = PageDTO.of(query.getPage(), query.getSize());

        LambdaQueryWrapper<E> lambda = this.setBasicCondition(query);
        this.customConditions(lambda, query);

        Page<E> selected = this.getBaseMapper()
                               .selectPage(page, lambda);
        return PagingVo.of(selected.getCurrent(), selected.getSize(), selected.getTotal(), selected.getRecords());
    }

    /**
     设置基础的查询条件
     */
    protected LambdaQueryWrapper<E> setBasicCondition(BasicQuery query) {
        // 查询条件
        Instant startsAt = query.getStartsAt();
        Instant endsAt = query.getEndsAt();
        List<String> sorts = query.getSorts();

        QueryWrapper<E> queryWrapper = Wrappers.query();
        queryWrapper.ge(Objects.nonNull(startsAt), BuiltInColumnConst.CREATE_TIME, startsAt)
                    .le(Objects.nonNull(endsAt), BuiltInColumnConst.CREATE_TIME, endsAt);

        if (Objects.isNull(sorts)) {
            queryWrapper.orderByDesc(BuiltInColumnConst.CREATE_TIME);
        } else {
            List<String> legalSortFields = query.obtainLegalSortFields();

            for (String sort : sorts) {
                Matcher matcher = sortFieldPattern.matcher(sort);
                if (matcher.find()) {
                    String field = matcher.group(1);
                    String direction = matcher.group(3);
                    // 合法的排序字段判断
                    boolean contains = legalSortFields.contains(field);
                    Asserts.isTrue(contains, ErrorCodeEnum.PARAMS_INCORRECT, "非法的排序字段!!");

                    boolean isAsc = Objects.isNull(direction) || "asc".equalsIgnoreCase(direction);
                    queryWrapper.orderBy(true, isAsc, field);
                }
            }
        }

        return queryWrapper.lambda();
    }

    /**
     添加其他分页条件, 若无, 则空实现即可

     @param wrapper {@link LambdaQueryWrapper}
     @param query   其他查询条件
     */
    protected void customConditions(LambdaQueryWrapper<E> wrapper, Q query) {}

}
