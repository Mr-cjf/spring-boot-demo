package top.cjf_rb.mp.constant;

/**
 内置列名常量

 @author cjf
 @since 1.0 */
public final class BuiltInColumnConst {
    /**
     乐观锁
     */
    public static final String OPTIMISTIC_LOCK = "version";
    /**
     逻辑删除
     */
    public static final String LOGICAL_DELETION = "is_deleted";
    /**
     创建者ID
     */
    public static final String CREATOR_ID = "creator_id";
    /**
     创建时间
     */
    public static final String CREATE_TIME = "create_time";
    /**
     更新者ID
     */
    public static final String UPDATER_ID = "updater_Id";
    /**
     更新时间
     */
    public static final String UPDATE_TIME = "update_time";
    /**
     修改者ID
     */
    public static final String MODIFIER_ID = "modifier_id";
    /**
     修改时间
     */
    public static final String MODIFIER_TIME = "modifier_time";
    /**
     是否内置
     */
    public static final String BUILT_IN = "built_in";
}
