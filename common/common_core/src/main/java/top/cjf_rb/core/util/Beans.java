package top.cjf_rb.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Bean工具类
 */
@Slf4j
public final class Beans extends BeanUtils {


    public static <S, T> T copyProperties(S source, Supplier<T> target) {
        if (source == null) {
            return null;
        } return copyProperties(source, target, false, (String[]) null);
    }

    /**
     * 拷贝对象属性
     *
     * @param source 源对象
     * @param target 输出的对象
     */
    public static <S, T> T copyProperties(S source, Supplier<T> target, String... ignoreColumn) {
        String[] nullPropertyNames = getNullPropertyNames(source); if (ignoreColumn != null) {
            nullPropertyNames = ArrayUtils.insert(nullPropertyNames.length, nullPropertyNames, ignoreColumn);
        } T t = target.get(); copyProperties(source, t, nullPropertyNames); return t;
    }


    /**
     * 拷贝对象属性
     *
     * @param source 源对象
     * @param target 输出的对象
     */
    public static <S, T> T copyProperties(S source, Supplier<T> target, boolean ignoreNull, String... ignoreColumn) {
        if (ignoreNull) {
            String[] nullPropertyNames = getNullPropertyNames(source); if (ignoreColumn != null) {
                nullPropertyNames = ArrayUtils.insert(nullPropertyNames.length, nullPropertyNames, ignoreColumn);
            } T t = target.get(); copyProperties(source, t, nullPropertyNames); return t;
        } else {
            T t = target.get(); copyProperties(source, t, ignoreColumn); return t;
        }
    }

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source); PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>(); for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName()); if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        } String[] result = new String[emptyNames.size()]; return emptyNames.toArray(result);
    }

    /**
     * 拷贝对象属性
     *
     * @param source       源对象
     * @param targetClass  输出的对象Class
     * @param ignoreColumn 忽略的属性
     */
    public static <T> T copyPropertiesByClazz(Object source, Class<T> targetClass, String... ignoreColumn) {
        if (source == null) {
            return null;
        } String[] nullPropertyNames = getNullPropertyNames(source); if (ignoreColumn != null) {
            nullPropertyNames = ArrayUtils.insert(nullPropertyNames.length, nullPropertyNames, ignoreColumn);
        } T target = null; try {
            target = targetClass.getDeclaredConstructor()
                                .newInstance();
        } catch (Exception e) {
            log.error("copy class error", e);
        } assert target != null; copyProperties(source, target, nullPropertyNames); return target;
    }


    /**
     * 带回调函数的集合数据的拷贝（可自定义字段拷贝规则）
     *
     * @param sources 数据源类
     * @param target  目标类::new(eg: UserVO::new)
     */
    public static <S, T> List<T> copyListProperties(Collection<S> sources, Supplier<T> target) {
        return copyListProperties(sources, null, target, null);
    }

    /**
     * 集合数据的拷贝
     *
     * @param sources: 数据源类
     * @param target:  目标类::new(eg: UserVO::new)
     */
    public static <S, T, P> List<T> copyListProperties(List<S> sources, P p, Supplier<T> target) {
        return copyListProperties(sources, p, target, null);
    }

    /**
     * 带回调函数的集合数据的拷贝（可自定义字段拷贝规则）
     *
     * @param sources  数据源类
     * @param target   目标类::new(eg: UserVO::new)
     * @param callBack 回调函数
     * @param <T>      目标类
     * @return List<T>
     */
    public static <S, T, P> List<T> copyListProperties(List<S> sources, Supplier<T> target, BeanUtilCopyCallBack<S,
            T> callBack) {
        // 将sources列表中的元素复制到target中，并返回复制后的列表
        return copyListProperties(sources, null, target, callBack);
    }


    /**
     * 带回调函数的集合数据的拷贝（可自定义字段拷贝规则）
     *
     * @param sources  数据源类
     * @param p        源类
     * @param target   目标类::new(eg: UserVO::new)
     * @param callBack 回调函数
     * @param <S>      源类
     * @param <T>      目标类
     * @param <P>      源类
     * @return List<T>
     */
    public static <S, T, P> List<T> copyListProperties(Collection<S> sources, P p, Supplier<T> target,
                                                       BeanUtilCopyCallBack<S, T> callBack) {
        List<T> list = new ArrayList<>(sources.size()); for (S source : sources) {
            T t = target.get(); copyProperties(source, t); if (p != null) {
                copyProperties(p, t);
            } list.add(t); if (callBack != null) {
                // 回调
                callBack.callBack(source, t);
            }
        } return list;
    }

    /**
     * 集合转换成map
     *
     * @param list        集合
     * @param keyMapper   键
     * @param valueMapper 值
     * @param <T>         集合泛型
     * @param <K>         键泛型
     * @param <U>         值泛型
     */
    public static <T, K, U> Map<K, U> listToMap(List<T> list, Function<? super T, ? extends K> keyMapper, Function<?
            super T, ? extends U> valueMapper) {
        Map<K, U> map = new HashMap<>(); if (Nones.nonEmpty(list)) {
            map.putAll(list.stream()
                           .collect(Collectors.toMap(keyMapper, valueMapper)));
        } return map;
    }


    @FunctionalInterface
    public interface BeanUtilCopyCallBack<S, T> {
        /**
         * 定义默认回调方法
         *
         * @param t 目标对象
         * @param s 数据源对象
         */
        void callBack(S t, T s);
    }


}
