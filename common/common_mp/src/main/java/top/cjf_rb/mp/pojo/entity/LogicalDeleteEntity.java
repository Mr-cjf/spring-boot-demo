package top.cjf_rb.mp.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import top.cjf_rb.core.constant.AppSystemConst;

import java.io.Serial;
import java.io.Serializable;

/**
 逻辑删除的固定字段

 @author cjf */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class LogicalDeleteEntity<ID extends Serializable> extends BasicEntity<ID> {
    @Serial
    private static final long serialVersionUID = AppSystemConst.SERIAL_VERSION_UID;

    /**
     逻辑删除字段
     */
    @JsonIgnore
    @TableLogic
    @TableField(value = "is_deleted", select = false, fill = FieldFill.INSERT)
    private Boolean deleted;

}
