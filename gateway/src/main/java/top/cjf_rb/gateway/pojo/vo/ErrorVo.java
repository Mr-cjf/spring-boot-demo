package top.cjf_rb.gateway.pojo.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import top.cjf_rb.gateway.exception.ErrorCode;

import java.io.Serializable;

/**
 * 接口错误时统一的响应体
 * 
 * @author lty
 */
@Data
@Accessors(chain = true)
public class ErrorVo implements Serializable {

    /**
     * 状态码
     */
    private String code;
    /**
     * 返回信息
     */
    private String msg;
    /**
     * 额外数据
     */
    @Nullable
    private Object data;

    /**
     * 静态创建
     * 
     * @param errorCode 错误码
     * @param errorMsg 错误信息
     */
    public static ErrorVo of(@NonNull String errorCode, @NonNull String errorMsg) {
        return new ErrorVo().setCode(errorCode).setMsg(errorMsg);
    }

    public static <T extends ErrorCode> ErrorVo of(@NonNull T errorCode) {
        return new ErrorVo().setCode(errorCode.getCode()).setMsg(errorCode.getMsg());
    }

}
