package top.cjf_rb.mp.config;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import top.cjf_rb.core.constant.AppFieldConst;
import top.cjf_rb.core.context.UserContextHolder;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 自动填充功能详细 : <a href="https://baomidou.com/pages/4c6bcf/">自动填充功能</a>

 @author cjf */
public class MybatisPlusAutofillHandler implements MetaObjectHandler {
    static final String LOGICAL_DELETION_FIELD = AppFieldConst.LOGICAL_DELETION;
    static final String CREATOR_ID_FIELD = AppFieldConst.CREATOR_ID;
    static final String CREATE_TIME_FIELD = AppFieldConst.CREATE_TIME;
    static final String UPDATE_TIME_FIELD = AppFieldConst.UPDATE_TIME;

    /**
     插入元对象字段填充（用于插入时对公共字段的填充）

     @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        List<String> getterNames = Arrays.asList(metaObject.getGetterNames());
        boolean deletedContain = getterNames.contains(LOGICAL_DELETION_FIELD);
        if (deletedContain) {
            // 逻辑删除, 新增时默认为0
            this.strictInsertFill(metaObject, LOGICAL_DELETION_FIELD, () -> false, Boolean.class);
        }
        // 创建人
        boolean creatorContain = getterNames.contains(CREATOR_ID_FIELD);
        if (creatorContain) {
            long creatorId = UserContextHolder.getPrincipal();
            this.strictInsertFill(metaObject, CREATOR_ID_FIELD, () -> creatorId, Long.class);
        }
        // 创建时间
        boolean createTimeContain = getterNames.contains(CREATE_TIME_FIELD);
        if (createTimeContain) {
            this.strictInsertFill(metaObject, CREATE_TIME_FIELD, Instant::now, Instant.class);
        }
    }

    /**
     更新元对象字段填充（用于更新时对公共字段的填充）

     @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        List<String> getterNames = Arrays.asList(metaObject.getGetterNames());
        boolean updateContain = getterNames.contains(UPDATE_TIME_FIELD);
        if (updateContain) {
            this.strictUpdateFill(metaObject, UPDATE_TIME_FIELD, Instant::now, Instant.class);
        }
    }

}
