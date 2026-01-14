package top.cjf_rb.core.util;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 @author Zeno */
public class Actions {

    /**
     非空则执行
     */
    public static <T> void nonNullIfPresent(T object, Consumer<T> action) {
        if (Objects.nonNull(object)) {
            action.accept(object);
        }
    }

    /**
     非空则执行
     */
    public static void nonBlankIfPresent(String text, Consumer<String> action) {
        if (Nones.nonBlank(text)) {
            action.accept(text);
        }
    }

    /**
     非空则执行
     */
    public static <T> void nonEmptyIfPresent(T[] array, Consumer<T[]> action) {
        if (Nones.nonEmpty(array)) {
            action.accept(array);
        }
    }

    /**
     非空则执行
     */
    public static <T> void nonEmptyIfPresent(Collection<T> coll, Consumer<Collection<T>> action) {
        if (Nones.nonEmpty(coll)) {
            action.accept(coll);
        }
    }

    /**
     非空则执行
     */
    public static <K, V> void nonEmptyIfPresent(Map<K, V> map, Consumer<Map<K, V>> action) {
        if (Nones.nonEmpty(map)) {
            action.accept(map);
        }
    }

}
