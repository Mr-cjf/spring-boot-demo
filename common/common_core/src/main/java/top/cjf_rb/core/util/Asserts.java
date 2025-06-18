package top.cjf_rb.core.util;


import org.springframework.lang.Nullable;
import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.exception.AppException;

import java.util.Collection;
import java.util.Map;


/**
 * 断言工具类
 */
public final class Asserts {

    public static void isTrue(boolean expression, ErrorCodeEnum expCode) {
        if (!expression) {
            throw new AppException(expCode);
        }
    }

    public static void isTrue(boolean expression, ErrorCodeEnum expCode, Object data) {
        if (!expression) {
            throw new AppException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isTrue(boolean expression, ErrorCodeEnum expCode, String message) {
        if (!expression) {
            throw new AppException(expCode, message);
        }
    }

    public static void isTrue(boolean expression, ErrorCodeEnum expCode, String message, Object data) {
        if (!expression) {
            throw new AppException(expCode, message, data);
        }
    }

    public static void isFalse(boolean expression, ErrorCodeEnum expCode) {
        if (expression) {
            throw new AppException(expCode);
        }
    }

    public static void isFalse(boolean expression, ErrorCodeEnum expCode, Object data) {
        if (expression) {
            throw new AppException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isFalse(boolean expression, ErrorCodeEnum expCode, String message) {
        if (expression) {
            throw new AppException(expCode, message);
        }
    }

    public static void isFalse(boolean expression, ErrorCodeEnum expCode, String message, Object data) {
        if (expression) {
            throw new AppException(expCode, message, data);
        }
    }

    public static void isNull(@Nullable Object object, ErrorCodeEnum expCode) {
        if (object != null) {
            throw new AppException(expCode);
        }
    }

    public static void isNull(@Nullable Object object, ErrorCodeEnum expCode, Object data) {
        if (object != null) {
            throw new AppException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isNull(@Nullable Object object, ErrorCodeEnum expCode, String message) {
        if (object != null) {
            throw new AppException(expCode, message);
        }
    }

    public static void isNull(@Nullable Object object, ErrorCodeEnum expCode, String message, Object data) {
        if (object != null) {
            throw new AppException(expCode, message, data);
        }
    }

    public static void notNull(@Nullable Object object, ErrorCodeEnum expCode) {
        if (object == null) {
            throw new AppException(expCode);
        }
    }

    public static void notNull(@Nullable Object object, ErrorCodeEnum expCode, Object data) {
        if (object == null) {
            throw new AppException(expCode, expCode.getMsg(), data);
        }
    }

    public static void notNull(@Nullable Object object, ErrorCodeEnum expCode, String message) {
        if (object == null) {
            throw new AppException(expCode, message);
        }
    }

    public static void notNull(@Nullable Object object, ErrorCodeEnum expCode, String message, Object data) {
        if (object == null) {
            throw new AppException(expCode, message, data);
        }
    }

    public static void isBlank(@Nullable String string, ErrorCodeEnum expCode) {
        if (Nones.nonBlank(string)) {
            throw new AppException(expCode);
        }
    }

    public static void isBlank(@Nullable String string, ErrorCodeEnum expCode, Object data) {
        if (Nones.nonBlank(string)) {
            throw new AppException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isBlank(@Nullable String string, ErrorCodeEnum expCode, String message) {
        if (Nones.nonBlank(string)) {
            throw new AppException(expCode, message);
        }
    }

    public static void isBlank(@Nullable String string, ErrorCodeEnum expCode, String message, Object data) {
        if (Nones.nonBlank(string)) {
            throw new AppException(expCode, message, data);
        }
    }

    public static void notBlank(@Nullable String string, ErrorCodeEnum expCode) {
        if (Nones.isBlank(string)) {
            throw new AppException(expCode);
        }
    }

    public static void notBlank(@Nullable String string, ErrorCodeEnum expCode, Object data) {
        if (Nones.isBlank(string)) {
            throw new AppException(expCode, expCode.getMsg(), data);
        }
    }

    public static void notBlank(@Nullable String string, ErrorCodeEnum expCode, String message) {
        if (Nones.isBlank(string)) {
            throw new AppException(expCode, message);
        }
    }

    public static void notBlank(@Nullable String string, ErrorCodeEnum expCode, String message, Object data) {
        if (Nones.isBlank(string)) {
            throw new AppException(expCode, message, data);
        }
    }

    public static void isEmpty(@Nullable Object[] array, ErrorCodeEnum expCode) {
        if (Nones.nonEmpty(array)) {
            throw new AppException(expCode);
        }
    }

    public static void isEmpty(@Nullable Object[] array, ErrorCodeEnum expCode, Object data) {
        if (Nones.nonEmpty(array)) {
            throw new AppException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isEmpty(@Nullable Object[] array, ErrorCodeEnum expCode, String message) {
        if (Nones.nonEmpty(array)) {
            throw new AppException(expCode, message);
        }
    }

    public static void isEmpty(@Nullable Object[] array, ErrorCodeEnum expCode, String message, Object data) {
        if (Nones.nonEmpty(array)) {
            throw new AppException(expCode, message, data);
        }
    }

    public static void isEmpty(@Nullable Collection<?> list, ErrorCodeEnum expCode) {
        if (Nones.nonEmpty(list)) {
            throw new AppException(expCode);
        }
    }

    public static void isEmpty(@Nullable Collection<?> list, ErrorCodeEnum expCode, Object data) {
        if (Nones.nonEmpty(list)) {
            throw new AppException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isEmpty(@Nullable Collection<?> list, ErrorCodeEnum expCode, String message) {
        if (Nones.nonEmpty(list)) {
            throw new AppException(expCode, message);
        }
    }

    public static void isEmpty(@Nullable Collection<?> list, ErrorCodeEnum expCode, String message, Object data) {
        if (Nones.nonEmpty(list)) {
            throw new AppException(expCode, message, data);
        }
    }

    public static void isEmpty(@Nullable Map<?, ?> map, ErrorCodeEnum expCode) {
        if (Nones.nonEmpty(map)) {
            throw new AppException(expCode);
        }
    }

    public static void isEmpty(@Nullable Map<?, ?> map, ErrorCodeEnum expCode, Object data) {
        if (Nones.nonEmpty(map)) {
            throw new AppException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isEmpty(@Nullable Map<?, ?> map, ErrorCodeEnum expCode, String message) {
        if (Nones.nonEmpty(map)) {
            throw new AppException(expCode, message);
        }
    }

    public static void isEmpty(@Nullable Map<?, ?> map, ErrorCodeEnum expCode, String message, Object data) {
        if (Nones.nonEmpty(map)) {
            throw new AppException(expCode, message, data);
        }
    }

    public static void notEmpty(@Nullable Object[] array, ErrorCodeEnum expCode) {
        if (Nones.isEmpty(array)) {
            throw new AppException(expCode);
        }
    }

    public static void notEmpty(@Nullable Object[] array, ErrorCodeEnum expCode, Object data) {
        if (Nones.isEmpty(array)) {
            throw new AppException(expCode, expCode.getMsg(), data);
        }
    }

    public static void notEmpty(@Nullable Object[] array, ErrorCodeEnum expCode, String message) {
        if (Nones.isEmpty(array)) {
            throw new AppException(expCode, message);
        }
    }

    public static void notEmpty(@Nullable Object[] array, ErrorCodeEnum expCode, String message, Object data) {
        if (Nones.isEmpty(array)) {
            throw new AppException(expCode, message, data);
        }
    }

    public static void notEmpty(@Nullable Collection<?> list, ErrorCodeEnum expCode) {
        if (Nones.isEmpty(list)) {
            throw new AppException(expCode);
        }
    }

    public static void notEmpty(@Nullable Collection<?> list, ErrorCodeEnum expCode, Object data) {
        if (Nones.isEmpty(list)) {
            throw new AppException(expCode, expCode.getMsg(), data);
        }
    }

    public static void notEmpty(@Nullable Collection<?> list, ErrorCodeEnum expCode, String message) {
        if (Nones.isEmpty(list)) {
            throw new AppException(expCode, message);
        }
    }

    public static void notEmpty(@Nullable Collection<?> list, ErrorCodeEnum expCode, String message, Object data) {
        if (Nones.isEmpty(list)) {
            throw new AppException(expCode, message, data);
        }
    }

    public static void notEmpty(@Nullable Map<?, ?> map, ErrorCodeEnum expCode) {
        if (Nones.isEmpty(map)) {
            throw new AppException(expCode);
        }
    }

    public static void notEmpty(@Nullable Map<?, ?> map, ErrorCodeEnum expCode, Object data) {
        if (Nones.isEmpty(map)) {
            throw new AppException(expCode, expCode.getMsg(), data);
        }
    }

    public static void notEmpty(@Nullable Map<?, ?> map, ErrorCodeEnum expCode, String message) {
        if (Nones.isEmpty(map)) {
            throw new AppException(expCode, message);
        }
    }

    public static void notEmpty(@Nullable Map<?, ?> map, ErrorCodeEnum expCode, String message, Object data) {
        if (Nones.isEmpty(map)) {
            throw new AppException(expCode, message, data);
        }
    }

}
