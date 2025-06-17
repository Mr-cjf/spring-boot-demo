package top.cjf_rb.core.exception;


import lombok.Getter;
import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.constant.SeparatorEnum;

import java.util.function.BiFunction;

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
        super(String.join(DELIMITER, errorCodeEnum.getCode(), errorCodeEnum.getMsg()), throwable, true, true);
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
        super(String.join(DELIMITER, errorCodeEnum.getCode(), errorCodeEnum.getMsg(), description), throwable, true,
                true);
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

    /**
     * 从源异常构建目标异常实例
     *
     * @param source      原始异常
     * @param constructor 构造函数 (code, description) -> T
     * @return 构造后的异常对象
     */
    public static <T extends AppException> T buildFromAppException(AppException source, BiFunction<ErrorCodeEnum,
            String, T> constructor) {
        T result;

        if (source.getData() != null) {
            result = constructor.apply(source.getErrorCodeEnum(), source.getDescription());
        } else if (source.getDescription() != null) {
            result = constructor.apply(source.getErrorCodeEnum(), source.getDescription());
        } else {
            result = constructor.apply(source.getErrorCodeEnum(), null);
        }

        // ⚠️ 关键步骤：保留原始异常作为 cause
        result.initCause(source.getCause());

        return result;
    }

    /**
     * 处理任意异常并返回目标类型的 AppException 实例（统一入口）
     *
     * @param t              输入异常（Throwable）
     * @param exceptionClass 目标异常类型
     * @param constructor    构造函数 (code, description) -> T
     * @return 构造后的目标异常
     */
    private static <T extends AppException> T handleExceptionInternal(
            Throwable t,
            Class<T> exceptionClass,
            BiFunction<ErrorCodeEnum, String, T> constructor) {

        // 如果已经是目标类型，直接返回
        if (exceptionClass.isInstance(t)) {
            return exceptionClass.cast(t);
        }

        // 如果是 AppException 或其子类，则进行转换
        if (t instanceof AppException appException) {
            return buildFromAppException(appException, constructor);
        }

        // 其他异常包装为 UNKNOWN_ERROR
        AppException unknown = new AppException(ErrorCodeEnum.UNKNOWN_ERROR, t.getMessage(), t);
        return buildFromAppException(unknown, constructor);
    }

    /**
     * 处理任意异常并返回目标类型的 AppException 实例（支持 Exception 参数）
     *
     * @param ex             输入异常
     * @param exceptionClass 目标异常类型
     * @param constructor    构造函数
     * @return 构造后的目标异常
     */
    public static <T extends AppException> T handleSubClassException(
            Exception ex,
            Class<T> exceptionClass,
            BiFunction<ErrorCodeEnum, String, T> constructor) {
        return handleExceptionInternal(ex, exceptionClass, constructor);
    }

    /**
     * 处理任意异常并返回目标类型的 AppException 实例（支持 Throwable 参数）
     *
     * @param t              输入异常（Throwable）
     * @param exceptionClass 目标异常类型
     * @param constructor    构造函数
     * @return 构造后的目标异常
     */
    public static <T extends AppException> T handleSubClassException(
            Throwable t,
            Class<T> exceptionClass,
            BiFunction<ErrorCodeEnum, String, T> constructor) {
        return handleExceptionInternal(t, exceptionClass, constructor);
    }


}

