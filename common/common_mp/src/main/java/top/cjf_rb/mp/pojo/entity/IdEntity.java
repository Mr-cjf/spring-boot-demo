package top.cjf_rb.mp.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;
import top.cjf_rb.core.constant.AppSystemConst;

import java.io.Serial;
import java.io.Serializable;

/**
 基础的数据表实体类

 @author cjf
 @since 1.0 */
@Data
@Accessors(chain = true)
public abstract class IdEntity<ID extends Serializable> implements Serializable {
    @Serial
    private static final long serialVersionUID = AppSystemConst.SERIAL_VERSION_UID;

    /**
     ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private ID id;

}
