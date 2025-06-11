package top.cjf_rb.core.context.type;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import top.cjf_rb.core.constant.AppSystemConst;

import java.io.Serial;
import java.io.Serializable;

/**
 * 加密字符串
 *
 * @author lty
 * @since 1.0
 */
@Getter
@EqualsAndHashCode
public final class Ciphertext implements Serializable {
    @Serial
    private static final long serialVersionUID = AppSystemConst.SERIAL_VERSION_UID;

    private final String value;

    public Ciphertext(String string) {
        this.value = string;
    }

    @Override
    public String toString() {
        return value;
    }

}
