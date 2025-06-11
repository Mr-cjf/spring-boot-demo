package top.cjf_rb.core.exception;


import lombok.Getter;
import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.constant.SeparatorEnum;

/**
 * 业务异常
 */
@Getter
public class AppException extends RuntimeException {

    private static final String DELIMITER = SeparatorEnum.SPACE.getSeparator();

    private final ErrorCodeEnum errorCodeEnum;

    private Object data;

    private String description;

    /**
     * 构造函数，传入错误码
     *
     * @param errorCodeEnum 错误码
     */
    public AppException(ErrorCodeEnum errorCodeEnum) {
        // 调用父类的构造函数，传入错误码和错误信息
        super(String.join(DELIMITER, errorCodeEnum.getCode(), errorCodeEnum.getMsg()));
        // 将错误码赋值给成员变量
        this.errorCodeEnum = errorCodeEnum;
    }

    /**
     * 构造函数，传入错误码和描述
     *
     * @param errorCodeEnum 错误码
     * @param description   描述
     */
    public AppException(ErrorCodeEnum errorCodeEnum, String description) {
        // 调用父类的构造函数，传入错误码、错误信息和描述，用DELIMITER分隔
        super(String.join(DELIMITER, errorCodeEnum.getCode(), errorCodeEnum.getMsg(), description));
        // 将错误码和描述赋值给成员变量
        this.errorCodeEnum = errorCodeEnum;
        this.description = description;
    }

    /**
     * 构造函数，传入错误码和异常
     *
     * @param errorCodeEnum 错误码
     * @param throwable     异常
     */
    public AppException(ErrorCodeEnum errorCodeEnum, Throwable throwable) {
        // 调用父类的构造函数，传入错误码和异常
        super(String.join(DELIMITER, errorCodeEnum.getCode(), errorCodeEnum.getMsg()), throwable);
        // 将错误码赋值给成员变量
        this.errorCodeEnum = errorCodeEnum;
    }

    /**
     * 构造函数，传入错误码、描述和异常
     *
     * @param errorCodeEnum 错误码
     * @param description   描述
     * @param throwable     异常
     */
    public AppException(ErrorCodeEnum errorCodeEnum, String description, Throwable throwable) {
        // 调用父类的构造函数，传入错误码、描述和异常
        super(String.join(DELIMITER, errorCodeEnum.getCode(), errorCodeEnum.getMsg(), description), throwable);
        // 设置错误码
        this.errorCodeEnum = errorCodeEnum;
        // 设置描述
        this.description = description;
    }

    /**
     * 构造函数，传入错误码、描述和数据
     *
     * @param errorCodeEnum 错误码
     * @param description   描述
     * @param data          数据
     */
    public AppException(ErrorCodeEnum errorCodeEnum, String description, Object data) {
        // 调用父类的构造函数，传入错误码、错误信息和描述
        super(String.join(DELIMITER, errorCodeEnum.getCode(), errorCodeEnum.getMsg(), description));
        // 设置错误码
        this.errorCodeEnum = errorCodeEnum;
        // 设置描述
        this.description = description;
        // 设置数据
        this.data = data;
    }

}

