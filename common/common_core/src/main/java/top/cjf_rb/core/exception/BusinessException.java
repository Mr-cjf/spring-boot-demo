package top.cjf_rb.core.exception;


import lombok.Getter;
import top.cjf_rb.core.constant.ErrorCodeEnum;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends AppException {


    /**
     * 构造函数，传入错误码
     *
     * @param errorCodeEnum 错误码
     */
    public BusinessException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum);
    }

    /**
     * 构造函数，传入错误码和描述
     *
     * @param errorCodeEnum 错误码
     * @param description   描述
     */
    public BusinessException(ErrorCodeEnum errorCodeEnum, String description) {
        super(errorCodeEnum, description);
    }

    /**
     * 构造函数，传入错误码和异常
     *
     * @param errorCodeEnum 错误码
     * @param throwable     异常
     */
    public BusinessException(ErrorCodeEnum errorCodeEnum, Throwable throwable) {
        super(errorCodeEnum, throwable);
    }

    /**
     * 构造函数，传入错误码、描述和异常
     *
     * @param errorCodeEnum 错误码
     * @param description   描述
     * @param throwable     异常
     */
    public BusinessException(ErrorCodeEnum errorCodeEnum, String description, Throwable throwable) {
        super(errorCodeEnum, description, throwable);
    }

    /**
     * 构造函数，传入错误码、描述和数据
     *
     * @param errorCodeEnum 错误码
     * @param description   描述
     * @param data          数据
     */
    public BusinessException(ErrorCodeEnum errorCodeEnum, String description, Object data) {
        super(errorCodeEnum, description, data);
    }
}

