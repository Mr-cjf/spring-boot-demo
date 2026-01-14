package top.cjf_rb.mp.pojo.entity;

import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 乐观锁字段的实体

 @author cjf */
@Data
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class OptimisticLockEntity<ID extends Serializable> extends LogicalDeleteEntity<ID> {

    /**
     乐观锁标识字段
     */
    @Version
    @JsonIgnore
    private Long version;

}
