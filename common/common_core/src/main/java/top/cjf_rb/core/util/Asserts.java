package top.cjf_rb.core.util;


import org.springframework.lang.Nullable;
import top.cjf_rb.core.exception.BusinessException;
import top.cjf_rb.core.exception.ErrorCode;

import java.util.Collection;
import java.util.Map;


/**
 * @author Zeno
 */
public class Asserts {

    public static void isTrue(boolean expression, ErrorCode expCode) {
        if (!expression) {
            throw new BusinessException(expCode);
        }
    }

    public static void isTrue(boolean expression, ErrorCode expCode, Object data) {
        if (!expression) {
            throw new BusinessException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isTrue(boolean expression, ErrorCode expCode, String message) {
        if (!expression) {
            throw new BusinessException(expCode, message);
        }
    }

    public static void isTrue(boolean expression, ErrorCode expCode, String message, Object data) {
        if (!expression) {
            throw new BusinessException(expCode, message, data);
        }
    }

    public static void isFalse(boolean expression, ErrorCode expCode) {
        if (expression) {
            throw new BusinessException(expCode);
        }
    }

    public static void isFalse(boolean expression, ErrorCode expCode, Object data) {
        if (expression) {
            throw new BusinessException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isFalse(boolean expression, ErrorCode expCode, String message) {
        if (expression) {
            throw new BusinessException(expCode, message);
        }
    }

    public static void isFalse(boolean expression, ErrorCode expCode, String message, Object data) {
        if (expression) {
            throw new BusinessException(expCode, message, data);
        }
    }

    public static void isNull(@Nullable Object object, ErrorCode expCode) {
        if (object != null) {
            throw new BusinessException(expCode);
        }
    }

    public static void isNull(@Nullable Object object, ErrorCode expCode, Object data) {
        if (object != null) {
            throw new BusinessException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isNull(@Nullable Object object, ErrorCode expCode, String message) {
        if (object != null) {
            throw new BusinessException(expCode, message);
        }
    }

    public static void isNull(@Nullable Object object, ErrorCode expCode, String message, Object data) {
        if (object != null) {
            throw new BusinessException(expCode, message, data);
        }
    }

    public static void notNull(@Nullable Object object, ErrorCode expCode) {
        if (object == null) {
            throw new BusinessException(expCode);
        }
    }

    public static void notNull(@Nullable Object object, ErrorCode expCode, Object data) {
        if (object == null) {
            throw new BusinessException(expCode, expCode.getMsg(), data);
        }
    }

    public static void notNull(@Nullable Object object, ErrorCode expCode, String message) {
        if (object == null) {
            throw new BusinessException(expCode, message);
        }
    }

    public static void notNull(@Nullable Object object, ErrorCode expCode, String message, Object data) {
        if (object == null) {
            throw new BusinessException(expCode, message, data);
        }
    }

    public static void isBlank(@Nullable String string, ErrorCode expCode) {
        if (Nones.nonBlank(string)) {
            throw new BusinessException(expCode);
        }
    }

    public static void isBlank(@Nullable String string, ErrorCode expCode, Object data) {
        if (Nones.nonBlank(string)) {
            throw new BusinessException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isBlank(@Nullable String string, ErrorCode expCode, String message) {
        if (Nones.nonBlank(string)) {
            throw new BusinessException(expCode, message);
        }
    }

    public static void isBlank(@Nullable String string, ErrorCode expCode, String message, Object data) {
        if (Nones.nonBlank(string)) {
            throw new BusinessException(expCode, message, data);
        }
    }

    public static void notBlank(@Nullable String string, ErrorCode expCode) {
        if (Nones.isBlank(string)) {
            throw new BusinessException(expCode);
        }
    }

    public static void notBlank(@Nullable String string, ErrorCode expCode, Object data) {
        if (Nones.isBlank(string)) {
            throw new BusinessException(expCode, expCode.getMsg(), data);
        }
    }

    public static void notBlank(@Nullable String string, ErrorCode expCode, String message) {
        if (Nones.isBlank(string)) {
            throw new BusinessException(expCode, message);
        }
    }

    public static void notBlank(@Nullable String string, ErrorCode expCode, String message, Object data) {
        if (Nones.isBlank(string)) {
            throw new BusinessException(expCode, message, data);
        }
    }

    public static void isEmpty(@Nullable Object[] array, ErrorCode expCode) {
        if (Nones.nonEmpty(array)) {
            throw new BusinessException(expCode);
        }
    }

    public static void isEmpty(@Nullable Object[] array, ErrorCode expCode, Object data) {
        if (Nones.nonEmpty(array)) {
            throw new BusinessException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isEmpty(@Nullable Object[] array, ErrorCode expCode, String message) {
        if (Nones.nonEmpty(array)) {
            throw new BusinessException(expCode, message);
        }
    }

    public static void isEmpty(@Nullable Object[] array, ErrorCode expCode, String message, Object data) {
        if (Nones.nonEmpty(array)) {
            throw new BusinessException(expCode, message, data);
        }
    }

    public static void isEmpty(@Nullable Collection<?> list, ErrorCode expCode) {
        if (Nones.nonEmpty(list)) {
            throw new BusinessException(expCode);
        }
    }

    public static void isEmpty(@Nullable Collection<?> list, ErrorCode expCode, Object data) {
        if (Nones.nonEmpty(list)) {
            throw new BusinessException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isEmpty(@Nullable Collection<?> list, ErrorCode expCode, String message) {
        if (Nones.nonEmpty(list)) {
            throw new BusinessException(expCode, message);
        }
    }

    public static void isEmpty(@Nullable Collection<?> list, ErrorCode expCode, String message, Object data) {
        if (Nones.nonEmpty(list)) {
            throw new BusinessException(expCode, message, data);
        }
    }

    public static void isEmpty(@Nullable Map<?, ?> map, ErrorCode expCode) {
        if (Nones.nonEmpty(map)) {
            throw new BusinessException(expCode);
        }
    }

    public static void isEmpty(@Nullable Map<?, ?> map, ErrorCode expCode, Object data) {
        if (Nones.nonEmpty(map)) {
            throw new BusinessException(expCode, expCode.getMsg(), data);
        }
    }

    public static void isEmpty(@Nullable Map<?, ?> map, ErrorCode expCode, String message) {
        if (Nones.nonEmpty(map)) {
            throw new BusinessException(expCode, message);
        }
    }

    public static void isEmpty(@Nullable Map<?, ?> map, ErrorCode expCode, String message, Object data) {
        if (Nones.nonEmpty(map)) {
            throw new BusinessException(expCode, message, data);
        }
    }

    public static void notEmpty(@Nullable Object[] array, ErrorCode expCode) {
        if (Nones.isEmpty(array)) {
            throw new BusinessException(expCode);
        }
    }

    public static void notEmpty(@Nullable Object[] array, ErrorCode expCode, Object data) {
        if (Nones.isEmpty(array)) {
            throw new BusinessException(expCode, expCode.getMsg(), data);
        }
    }

    public static void notEmpty(@Nullable Object[] array, ErrorCode expCode, String message) {
        if (Nones.isEmpty(array)) {
            throw new BusinessException(expCode, message);
        }
    }

    public static void notEmpty(@Nullable Object[] array, ErrorCode expCode, String message, Object data) {
        if (Nones.isEmpty(array)) {
            throw new BusinessException(expCode, message);
        }
    }

    public static void notEmpty(@Nullable Collection<?> list, ErrorCode expCode) {
        if (Nones.isEmpty(list)) {
            throw new BusinessException(expCode);
        }
    }

    public static void notEmpty(@Nullable Collection<?> list, ErrorCode expCode, Object data) {
        if (Nones.isEmpty(list)) {
            throw new BusinessException(expCode, expCode.getMsg(), data);
        }
    }

    public static void notEmpty(@Nullable Collection<?> list, ErrorCode expCode, String message) {
        if (Nones.isEmpty(list)) {
            throw new BusinessException(expCode, message);
        }
    }

    public static void notEmpty(@Nullable Collection<?> list, ErrorCode expCode, String message, Object data) {
        if (Nones.isEmpty(list)) {
            throw new BusinessException(expCode, message, data);
        }
    }

    public static void notEmpty(@Nullable Map<?, ?> map, ErrorCode expCode) {
        if (Nones.isEmpty(map)) {
            throw new BusinessException(expCode);
        }
    }

    public static void notEmpty(@Nullable Map<?, ?> map, ErrorCode expCode, Object data) {
        if (Nones.isEmpty(map)) {
            throw new BusinessException(expCode, expCode.getMsg(), data);
        }
    }

    public static void notEmpty(@Nullable Map<?, ?> map, ErrorCode expCode, String message) {
        if (Nones.isEmpty(map)) {
            throw new BusinessException(expCode, message);
        }
    }

    public static void notEmpty(@Nullable Map<?, ?> map, ErrorCode expCode, String message, Object data) {
        if (Nones.isEmpty(map)) {
            throw new BusinessException(expCode, message, data);
        }
    }

}
