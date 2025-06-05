package top.cjf_rb.core.exception;


import lombok.Getter;
import top.cjf_rb.core.constant.AppSeparatorConst;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final String DELIMITER = AppSeparatorConst.SPACE.getSeparator();

    private final ErrorCode errorCode;

    private Object data;

    private String description;

    public BusinessException(ErrorCode errorCode) {
        super(String.join(DELIMITER, errorCode.getCode(), errorCode.getMsg()));
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(String.join(DELIMITER, errorCode.getCode(), errorCode.getMsg(), description));
        this.errorCode = errorCode;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode, Throwable throwable) {
        super(String.join(DELIMITER, errorCode.getCode(), errorCode.getMsg()), throwable);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String description, Throwable throwable) {
        super(String.join(DELIMITER, errorCode.getCode(), errorCode.getMsg(), description), throwable);
        this.errorCode = errorCode;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode, String description, Object data) {
        super(String.join(DELIMITER, errorCode.getCode(), errorCode.getMsg(), description));
        this.errorCode = errorCode;
        this.description = description;
        this.data = data;
    }

}

