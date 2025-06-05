package top.cjf_rb.core.util;

import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 统一空判断的代码风格, 避免项目中过多引用第三方的空判断工具类
 */
public final class Nones {

    /**
     * 判断Object是否为null
     *
     * @param object object to be judged
     * @return if null return true, or false
     */
    public static boolean isNull(Object object) {
        return Objects.isNull(object);
    }

    /**
     * 判断Object是否不为null
     *
     * @param object object to be judged
     * @return if null return false, or true
     */
    public static boolean nonNull(Object object) {
        return Objects.nonNull(object);
    }

    /**
     * 判断String是否为null | '' | " "等空白
     *
     * @param text string to be judged
     * @return if blank return true, or false
     */
    public static boolean isBlank(String text) {
        return !StringUtils.hasText(text);
    }

    /**
     * 判断String是否为null | '' | " "等空白
     *
     * @param text string to be judged
     * @return if blank return true, or false
     */
    public static boolean nonBlank(String text) {
        return StringUtils.hasText(text);
    }

    /**
     * 判断String是否为null | '' | " "等空白
     *
     * @param text string to be judged
     * @throws IllegalArgumentException if {@code text} is {@code null or blank}
     */
    public static void requireNonBlank(String text) {
        if (isBlank(text)) {
            throw new IllegalArgumentException("The parameter must not be null or blank!");
        }
    }

    /**
     * 判断Array是否为null || 空数组(无元素)
     *
     * @param array array to be judged
     * @return if empty return true, or false
     */
    public static boolean isEmpty(Object[] array) {
        return Objects.isNull(array) || array.length == 0;
    }

    /**
     * 判断Array是否不为null || 空数组(无元素)
     *
     * @param array array to be judged
     * @return if empty return false, or true
     */
    public static boolean nonEmpty(Object[] array) {
        return !isEmpty(array);
    }

    /**
     * 判断Array是否不为null || 空数组(无元素)
     *
     * @param array array to be judged
     * @throws IllegalArgumentException if {@code array} is {@code null or empty}
     */
    public static void requireNonEmpty(Object[] array) {
        if (isEmpty(array)) {
            throw new IllegalArgumentException("The parameter must not be null or empty!");
        }
    }

    /**
     * 判断Collection集合是否为null || 空集合(无元素)
     *
     * @param coll collection to be judged
     * @return if empty return true, or false
     */
    public static boolean isEmpty(Collection<?> coll) {
        return Objects.isNull(coll) || coll.isEmpty();
    }

    /**
     * 判断Collection集合是否不为null || 空集合(无元素)
     *
     * @param coll collection to be judged
     * @return if empty return false, or true
     */
    public static boolean nonEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    /**
     * 判断Collection集合是否不为null || 空集合(无元素)
     *
     * @param coll collection to be judged
     * @throws IllegalArgumentException if {@code coll} is {@code null or empty}
     */
    public static void requireNonEmpty(Collection<?> coll) {
        if (isEmpty(coll)) {
            throw new IllegalArgumentException("The parameter must not be null or empty!");
        }
    }

    /**
     * 判断Map集合是否为null || 空集合(无元素)
     *
     * @param map map to be judged
     * @return if empty return true, or false
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return Objects.isNull(map) || map.isEmpty();
    }

    /**
     * 判断Map集合是否不为null || 空集合(无元素)
     *
     * @param map map to be judged
     * @return if empty return false, or true
     */
    public static boolean nonEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 判断Map集合是否不为null || 空集合(无元素)
     *
     * @param map map to be judged
     * @throws IllegalArgumentException if {@code coll} is {@code null or empty}
     */
    public static void requireNonEmpty(Map<?, ?> map) {
        if (isEmpty(map)) {
            throw new IllegalArgumentException("The parameter must not be null or empty!");
        }
    }

}

