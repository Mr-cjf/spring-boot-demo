package top.cjf_rb.mp.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;

/**
 基础的实体类

 @author cjf
 @since 1.0 */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BasicEntity<ID extends Serializable> extends IdEntity<ID> {

    /**
     创建人ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long creatorId;

    /**
     创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Instant createTime;

    /**
     更新时间
     */
    @TableField(fill = FieldFill.UPDATE)
    private Instant updateTime;

}
