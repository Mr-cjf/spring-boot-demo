package top.cjf_rb.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 字段匹配工具类
 提供通用的字段匹配和处理功能，业务逻辑由Consumer执行
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldMatchUtils {

    /**
     基本类型和包装类型的集合，用于快速判断是否需要继续递归处理
     */
    private static final Set<Class<?>> PRIMITIVE_LIKE = Set.of(String.class, Boolean.class, Character.class, Byte.class,
                                                               Short.class, Integer.class, Long.class, Float.class,
                                                               Double.class, Void.class);

    /**
     缓存类与敏感字段的 Field 对象映射关系，提高性能
     Key: 类的Class对象
     Value: 该类中所有被标记为敏感的字段列表
     */
    private static final Map<Class<?>, List<Field>> fieldCache = new ConcurrentHashMap<>();

    /**
     用于跟踪已处理的对象（按身份），防止循环引用导致的无限递归
     使用ThreadLocal确保线程安全
     */
    private static final ThreadLocal<Set<Object>> processedObjects = ThreadLocal.withInitial(
            () -> Collections.newSetFromMap(new IdentityHashMap<>()));

    /**
     缓存JsonPath字段路径匹配结果，提高性能
     Key: jsonPath + "|" + fieldPath
     Value: 对应的JsonPath值
     */
    private static final Map<String, List<Object>> jsonPathCache = new ConcurrentHashMap<>();

    /**
     遍历对象及其内部的所有字段，对匹配的字段执行指定的操作

     @param obj           需要处理的对象
     @param fieldNames    需要匹配的字段名称集合
     @param fieldConsumer 对匹配字段执行的操作，参数为(Field, Object instance, Object value)
     */
    public static void forEachMatchedField(Object obj, Set<String> fieldNames,
                                           TriConsumer<Field, Object, Object> fieldConsumer) {
        if (obj == null || fieldNames == null || fieldNames.isEmpty()) {
            return;
        }

        try {
            Set<Object> objects = processedObjects.get();
            objects.clear(); // 清空之前的数据
            forEachMatchedFieldInternal(obj, fieldNames, fieldConsumer, objects);
        } finally {
            processedObjects.get()
                            .clear(); // 确保清理
        }
    }

    /**
     递归遍历对象及其内部的所有字段，对匹配的字段执行指定的操作

     @param obj              需要处理的对象
     @param fieldNames       需要匹配的字段名称集合
     @param fieldConsumer    对匹配字段执行的操作，参数为(Field, Object instance, Object value)
     @param processedObjects 已处理的对象集合，防止循环引用
     */
    private static void forEachMatchedFieldInternal(Object obj, Set<String> fieldNames,
                                                    TriConsumer<Field, Object, Object> fieldConsumer,
                                                    Set<Object> processedObjects) {
        if (obj == null || fieldNames.isEmpty() || processedObjects.contains(obj)) {
            return;
        }

        processedObjects.add(obj);

        try {
            // 处理Map类型
            if (obj instanceof Map<?, ?> map) {
                map.forEach((k, v) -> {
                    forEachMatchedFieldInternal(k, fieldNames, fieldConsumer, processedObjects);
                    forEachMatchedFieldInternal(v, fieldNames, fieldConsumer, processedObjects);
                });
                return;
            }

            // 处理Collection类型
            if (handleCollectionType(obj, fieldNames, fieldConsumer, processedObjects)) {
                return;
            }

            // 处理数组类型
            if (handleArrayType(obj, fieldNames, fieldConsumer, processedObjects)) {
                return;
            }

            // 常见基本/包装类型或 Java 核心类，不再深入
            if (isPrimitiveOrWrapper(obj.getClass()) || isSpecialUnmodifiableClass(obj.getClass())) {
                return;
            }

            // 处理普通对象
            handleRegularObject(obj, fieldNames, fieldConsumer, processedObjects);
        } finally {
            processedObjects.remove(obj);
        }
    }

    /**
     处理集合类型的对象

     @param obj              集合对象
     @param fieldNames       需要匹配的字段名称集合
     @param fieldConsumer    对匹配字段执行的操作，参数为(Field, Object instance, Object value)
     @param processedObjects 已处理的对象集合
     @return 是否成功处理
     */
    private static boolean handleCollectionType(Object obj, Set<String> fieldNames,
                                                TriConsumer<Field, Object, Object> fieldConsumer,
                                                Set<Object> processedObjects) {
        if (obj instanceof Collection<?> collection) {
            for (Object item : collection) {
                forEachMatchedFieldInternal(item, fieldNames, fieldConsumer, processedObjects);
            }
            return true;
        }
        return false;
    }

    /**
     处理数组类型的对象

     @param obj              数组对象
     @param fieldNames       需要匹配的字段名称集合
     @param fieldConsumer    对匹配字段执行的操作，参数为(Field, Object instance, Object value)
     @param processedObjects 已处理的对象集合
     @return 是否成功处理
     */
    private static boolean handleArrayType(Object obj, Set<String> fieldNames,
                                           TriConsumer<Field, Object, Object> fieldConsumer,
                                           Set<Object> processedObjects) {
        Class<?> cls = obj.getClass();
        if (cls.isArray()) {
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                Object element = Array.get(obj, i);
                forEachMatchedFieldInternal(element, fieldNames, fieldConsumer, processedObjects);
            }
            return true;
        }
        return false;
    }

    /**
     处理普通 Java对象

     @param obj              普通 Java对象
     @param fieldNames       需要匹配的字段名称集合
     @param fieldConsumer    对匹配字段执行的操作，参数为(Field, Object instance, Object value)
     @param processedObjects 已处理的对象集合
     */
    private static void handleRegularObject(Object obj, Set<String> fieldNames,
                                            TriConsumer<Field, Object, Object> fieldConsumer,
                                            Set<Object> processedObjects) {
        Class<?> clazz = obj.getClass();
        List<Field> fields = getFieldsForClass(clazz, fieldNames);

        // 处理匹配的字段
        processMatchedFields(fields, obj, fieldConsumer);

        // 递归处理字段值（跳过静态/合成字段与 Java 核心/基本类型字段）
        processNestedFields(clazz, obj, fieldNames, fieldConsumer, processedObjects);
    }

    /**
     处理匹配的字段

     @param fields        匹配的字段列表
     @param obj           对象实例
     @param fieldConsumer 对匹配字段执行的操作
     */
    private static void processMatchedFields(List<Field> fields, Object obj,
                                             TriConsumer<Field, Object, Object> fieldConsumer) {
        for (Field field : fields) {
            try {
                ReflectionUtils.makeAccessible(field);
                Object value = ReflectionUtils.getField(field, obj);
                if (Nones.nonNull(value)) {
                    fieldConsumer.accept(field, obj, value);
                }
            } catch (Exception e) {
                logFieldProcessingError(field, e);
            }
        }
    }

    /**
     处理嵌套字段（递归处理）

     @param clazz            对象的类
     @param obj              对象实例
     @param fieldNames       需要匹配的字段名称集合
     @param fieldConsumer    对匹配字段执行的操作
     @param processedObjects 已处理的对象集合
     */
    private static void processNestedFields(Class<?> clazz, Object obj, Set<String> fieldNames,
                                            TriConsumer<Field, Object, Object> fieldConsumer,
                                            Set<Object> processedObjects) {
        ReflectionUtils.doWithFields(clazz, field -> {
            if (shouldSkipField(field)) {
                return;
            }
            try {
                Class<?> fieldType = field.getType();
                if (isSpecialUnmodifiableClass(fieldType) || isPrimitiveOrWrapper(fieldType)) {
                    return;
                }
                ReflectionUtils.makeAccessible(field);
                Object value = ReflectionUtils.getField(field, obj);
                if (Nones.nonNull(value)) {
                    forEachMatchedFieldInternal(value, fieldNames, fieldConsumer, processedObjects);
                }
            } catch (Exception e) {
                logFieldProcessingError(field, e);
            }
        });
    }

    /**
     判断是否应该跳过字段处理

     @param field 要检查的字段
     @return 是否应该跳过
     */
    private static boolean shouldSkipField(Field field) {
        return Modifier.isStatic(field.getModifiers()) || field.isSynthetic();
    }

    /**
     记录字段处理错误日志

     @param field 发生异常的字段
     @param e     异常对象
     */
    private static void logFieldProcessingError(Field field, Exception e) {
        log.warn("处理字段 {} 时发生异常: {}", field.getName(), e.getMessage(), e);
    }

    /**
     为类构建并缓存实际存在的指定字段列表

     @param clazz      目标类
     @param fieldNames 字段名称集合
     @return 匹配的字段列表
     */
    private static List<Field> getFieldsForClass(Class<?> clazz, Set<String> fieldNames) {
        return fieldCache.computeIfAbsent(clazz, k -> {
            List<Field> fields = new ArrayList<>();
            for (String fieldName : fieldNames) {
                if (fieldName == null || fieldName.isEmpty()) {
                    continue;
                }
                Field f = ReflectionUtils.findField(k, fieldName);
                if (f != null) {
                    ReflectionUtils.makeAccessible(f);
                    fields.add(f);
                }
            }
            return Collections.unmodifiableList(fields);
        });
    }

    /**
     判断是否为基本类型或包装类型

     @param clazz 要检查的类
     @return 是否为基本类型或包装类型
     */
    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() || PRIMITIVE_LIKE.contains(clazz);
    }

    /**
     判断是否为特殊的不可修改类（如Java核心类）

     @param clazz 要检查的类
     @return 是否为特殊的不可修改类
     */
    private static boolean isSpecialUnmodifiableClass(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }

        // 特殊处理：日期时间类不应被递归处理
        if (clazz == Date.class || java.time.temporal.Temporal.class.isAssignableFrom(clazz) || clazz.getName()
                                                                                                     .startsWith(
                                                                                                             "java.time.")) {
            return true;
        }

        String className = clazz.getName();

        // 检查是否为集合类型，集合类型不应被视为不可修改
        if (List.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz) ||
                Set.class.isAssignableFrom(clazz) || clazz.isArray()) {
            return false;
        }

        // 检查是否为基本包装类型（这些已经在isPrimitiveOrWrapper中检查过了）
        if (clazz.isPrimitive() || PRIMITIVE_LIKE.contains(clazz)) {
            return true;
        }

        // 检查是否为Java核心类
        return className.startsWith("java.lang") || className.startsWith("java.time") || className.startsWith(
                "java.math") || clazz.isEnum();
    }

    /**
     遍历对象及其内部的所有字段，对匹配的字段执行指定的操作

     @param obj           需要处理的对象
     @param fieldNames    需要匹配的字段名称集合
     @param jsonPath      JsonPath表达式，例如 "id", "records[*].id", "records[*].objs[*].id"
     @param fieldConsumer 对匹配字段执行的操作，参数为(Field, Object instance, Object value, List<Object> jsonValues)
     */
    public static void forEachMatchedField(Object obj, Set<String> fieldNames, String jsonPath,
                                           QuadConsumer<Field, Object, Object, List<Object>> fieldConsumer) {
        if (obj == null || fieldNames == null || fieldNames.isEmpty() || jsonPath == null) {
            return;
        }

        long startTime = System.currentTimeMillis();
        log.debug("开始处理对象字段匹配，fieldNames: {}，jsonPath: {}", fieldNames, jsonPath);

        try {
            Set<Object> objects = processedObjects.get();
            objects.clear(); // 清空之前的数据

            // 同时提取JsonPath值和查找匹配的敏感字段，提高性能
            Map<String, Object> pathToPathValue = new HashMap<>();
            long extractStartTime = System.currentTimeMillis();
            List<PathValueWithIndex> pathValuesWithIndex = getValuesByJsonPathWithIndex(obj, jsonPath);
            long extractTime = System.currentTimeMillis() - extractStartTime;

            for (PathValueWithIndex pv : pathValuesWithIndex) {
                if (pv.path != null) {
                    pathToPathValue.put(pv.path, pv.value);
                }
            }
            log.debug("JsonPath值提取耗时: {} ms，找到 {} 个路径值", extractTime, pathValuesWithIndex.size());

            // 使用优化的单次遍历方法查找匹配字段并进行关联
            findAndProcessMatchedFieldsWithJsonPath(obj, fieldNames, jsonPath, pathToPathValue, fieldConsumer);

        } finally {
            processedObjects.get()
                            .clear(); // 确保清理
            log.debug("字段匹配处理完成，总耗时: {} ms", System.currentTimeMillis() - startTime);
        }
    }

    /**
     查找并处理匹配的字段，同时进行JsonPath关联（优化版，单次遍历）

     @param obj             需要处理的对象
     @param fieldNames      需要匹配的字段名称集合
     @param jsonPath        JsonPath表达式
     @param pathToPathValue JsonPath路径值映射
     @param fieldConsumer   对匹配字段执行的操作
     */
    private static void findAndProcessMatchedFieldsWithJsonPath(Object obj, Set<String> fieldNames, String jsonPath,
                                                                Map<String, Object> pathToPathValue,
                                                                QuadConsumer<Field, Object, Object, List<Object>> fieldConsumer) {
        long startTime = System.currentTimeMillis();
        List<FieldWithValueWithPathAndMatch> matchedFieldsWithJsonPath = new ArrayList<>();

        // 通过单次遍历收集所有匹配字段及其 JsonPath关联
        collectMatchedFieldsWithPathAndJsonPath(obj, fieldNames, jsonPath, pathToPathValue, matchedFieldsWithJsonPath,
                                                "");

        log.debug("找到 {} 个匹配字段", matchedFieldsWithJsonPath.size());

        long processStartTime = System.currentTimeMillis();
        // 处理所有匹配的字段
        for (FieldWithValueWithPathAndMatch fieldWithPathAndMatch : matchedFieldsWithJsonPath) {
            fieldConsumer.accept(fieldWithPathAndMatch.field(), fieldWithPathAndMatch.instance(),
                                 fieldWithPathAndMatch.value(), fieldWithPathAndMatch.jsonPathValues());
        }
        long processTime = System.currentTimeMillis() - processStartTime;

        log.debug("处理匹配字段耗时: {} ms", processTime);

        log.debug("字段匹配总耗时: {} ms", System.currentTimeMillis() - startTime);
    }

    /**
     递归收集匹配的字段及其路径和JsonPath关联（优化版，单次遍历）
     */
    private static void collectMatchedFieldsWithPathAndJsonPath(Object obj, Set<String> fieldNames, String jsonPath,
                                                                Map<String, Object> pathToPathValue,
                                                                List<FieldWithValueWithPathAndMatch> result,
                                                                String currentPath) {
        if (obj == null || fieldNames.isEmpty() || processedObjects.get()
                                                                   .contains(obj)) {
            return;
        }

        processedObjects.get()
                        .add(obj);

        try {
            // 根据对象类型分发处理
            if (obj instanceof Map<?, ?>) {
                handleMapTypeForJsonPath((Map<?, ?>) obj, fieldNames, jsonPath, pathToPathValue, result, currentPath);
            } else if (obj instanceof Collection<?>) {
                handleCollectionTypeForJsonPath((Collection<?>) obj, fieldNames, jsonPath, pathToPathValue, result,
                                                currentPath);
            } else if (obj.getClass()
                          .isArray()) {
                handleArrayTypeForJsonPath(obj, fieldNames, jsonPath, pathToPathValue, result, currentPath);
            } else if (!(isPrimitiveOrWrapper(obj.getClass()) || isSpecialUnmodifiableClass(obj.getClass()))) {
                // 处理普通对象 (只有不是基本类型或特殊不可修改类时才处理)
                handleRegularObjectForJsonPath(obj, fieldNames, jsonPath, pathToPathValue, result, currentPath);
            }
        } finally {
            processedObjects.get()
                            .remove(obj);
        }
    }

    /**
     处理Map类型对象，收集匹配字段并关联JsonPath
     */
    private static void handleMapTypeForJsonPath(Map<?, ?> map, Set<String> fieldNames, String jsonPath,
                                                 Map<String, Object> pathToPathValue,
                                                 List<FieldWithValueWithPathAndMatch> result, String currentPath) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String keyPath = currentPath.isEmpty() ? entry.getKey()
                                                          .toString() : currentPath + "." + entry.getKey();

            // 检查Map的key是否匹配敏感字段名称
            if (fieldNames.contains(entry.getKey()
                                         .toString())) {
                List<Object> matchedJsonPathValues = findJsonPathValuesForFieldPath(jsonPath, pathToPathValue, keyPath);
                if (matchedJsonPathValues != null) {
                    result.add(new FieldWithValueWithPathAndMatch(null, entry.getValue(), map, keyPath,
                                                                  matchedJsonPathValues));
                }
            }

            // 递归处理Map的值，无论key是否匹配
            collectMatchedFieldsWithPathAndJsonPath(entry.getValue(), fieldNames, jsonPath, pathToPathValue, result,
                                                    keyPath);
        }
    }

    /**
     处理Collection类型对象，收集匹配字段并关联JsonPath
     */
    private static void handleCollectionTypeForJsonPath(Collection<?> collection, Set<String> fieldNames,
                                                        String jsonPath, Map<String, Object> pathToPathValue,
                                                        List<FieldWithValueWithPathAndMatch> result,
                                                        String currentPath) {
        int index = 0;
        for (Object item : collection) {
            String itemPath = currentPath + "[" + index + "]";
            collectMatchedFieldsWithPathAndJsonPath(item, fieldNames, jsonPath, pathToPathValue, result, itemPath);
            index++;
        }
    }

    /**
     处理数组类型对象，收集匹配字段并关联JsonPath
     */
    private static void handleArrayTypeForJsonPath(Object array, Set<String> fieldNames, String jsonPath,
                                                   Map<String, Object> pathToPathValue,
                                                   List<FieldWithValueWithPathAndMatch> result, String currentPath) {
        int len = Array.getLength(array);
        for (int i = 0; i < len; i++) {
            Object element = Array.get(array, i);
            String itemPath = currentPath + "[" + i + "]";
            collectMatchedFieldsWithPathAndJsonPath(element, fieldNames, jsonPath, pathToPathValue, result, itemPath);
        }
    }

    /**
     处理普通Java对象，收集匹配字段并关联JsonPath
     */
    private static void handleRegularObjectForJsonPath(Object obj, Set<String> fieldNames, String jsonPath,
                                                       Map<String, Object> pathToPathValue,
                                                       List<FieldWithValueWithPathAndMatch> result,
                                                       String currentPath) {
        Class<?> clazz = obj.getClass();

        // 处理匹配的字段
        processMatchedFieldsForJsonPath(clazz, obj, fieldNames, jsonPath, pathToPathValue, result, currentPath);

        // 递归处理所有字段值，查找嵌套的敏感字段
        processNestedFieldsForJsonPath(clazz, obj, fieldNames, jsonPath, pathToPathValue, result, currentPath);
    }

    /**
     处理匹配的字段，并关联JsonPath值
     */
    private static void processMatchedFieldsForJsonPath(Class<?> clazz, Object obj, Set<String> fieldNames,
                                                        String jsonPath, Map<String, Object> pathToPathValue,
                                                        List<FieldWithValueWithPathAndMatch> result,
                                                        String currentPath) {
        List<Field> fields = getFieldsForClass(clazz, fieldNames);

        for (Field field : fields) {
            try {
                ReflectionUtils.makeAccessible(field);
                Object value = ReflectionUtils.getField(field, obj);
                if (Nones.nonNull(value)) {
                    String fieldPath = currentPath.isEmpty() ? field.getName() : currentPath + "." + field.getName();
                    List<Object> matchedJsonPathValues = findJsonPathValuesForFieldPath(jsonPath, pathToPathValue,
                                                                                        fieldPath);
                    if (matchedJsonPathValues != null) {
                        result.add(new FieldWithValueWithPathAndMatch(field, value, obj, fieldPath,
                                                                      matchedJsonPathValues));
                    }
                }
            } catch (Exception e) {
                logFieldProcessingError(field, e);
            }
        }
    }

    /**
     递归处理嵌套字段，查找匹配的敏感字段并关联JsonPath值
     */
    private static void processNestedFieldsForJsonPath(Class<?> clazz, Object obj, Set<String> fieldNames,
                                                       String jsonPath, Map<String, Object> pathToPathValue,
                                                       List<FieldWithValueWithPathAndMatch> result,
                                                       String currentPath) {
        ReflectionUtils.doWithFields(clazz, field -> {
            if (shouldSkipField(field)) {
                return;
            }

            try {
                Class<?> fieldType = field.getType();
                if (isSpecialUnmodifiableClass(fieldType) || isPrimitiveOrWrapper(fieldType)) {
                    return;
                }
                ReflectionUtils.makeAccessible(field);
                Object value = ReflectionUtils.getField(field, obj);
                if (Nones.nonNull(value)) {
                    String fieldPath = currentPath.isEmpty() ? field.getName() : currentPath + "." + field.getName();
                    collectMatchedFieldsWithPathAndJsonPath(value, fieldNames, jsonPath, pathToPathValue, result,
                                                            fieldPath);
                }
            } catch (Exception e) {
                logFieldProcessingError(field, e);
            }
        });
    }

    /**
     为字段路径查找对应的JsonPath值
     */
    private static List<Object> findJsonPathValuesForFieldPath(String jsonPath, Map<String, Object> pathToPathValue,
                                                               String fieldPath) {
        // 使用缓存避免重复计算
        String cacheKey = jsonPath + "|" + fieldPath;
        List<Object> cachedResult = jsonPathCache.get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        String jsonPathForField = extractJsonPathForField(jsonPath, fieldPath);
        List<Object> result;
        if (jsonPathForField != null && pathToPathValue.containsKey(jsonPathForField)) {
            result = Collections.singletonList(pathToPathValue.get(jsonPathForField));
        } else {
            result = null;
        }

        // 缓存结果
        jsonPathCache.put(cacheKey, result);
        return result;
    }

    /**
     从JsonPath和字段路径提取关联路径
     例如: jsonPath = "records[*].id", fieldPath = "records[0].mobile" -> "records[0].id"
     支持嵌套对象: jsonPath = "records[*].consultantSelfExamVo.id", fieldPath = "records[0].mobile" ->
     "records[0].consultantSelfExamVo.id"
     支持多级嵌套数组: jsonPath = "records[*].items[*].id", fieldPath = "records[0].items[1].name" -> "records[0].items[1].id"
     也支持简单路径: jsonPath = "id", fieldPath = "mobile" -> "id" (如果它们在同一对象层级)
     */
    private static String extractJsonPathForField(String jsonPath, String fieldPath) {
        if (jsonPath == null || fieldPath == null) {
            return null;
        }

        // 如果是简单路径匹配（没有[*]）
        if (!jsonPath.contains("[*]")) {
            return handleSimplePath(jsonPath, fieldPath);
        }

        // 处理包含[*]的路径，支持多级嵌套数组
        return handleArrayPath(jsonPath, fieldPath);
    }

    /**
     处理简单路径（不包含[*]）的情况
     */
    private static String handleSimplePath(String jsonPath, String fieldPath) {
        // 情况1: 如果jsonPath和fieldPath完全相同，直接返回
        if (jsonPath.equals(fieldPath)) {
            return jsonPath;
        }

        // 情况2: 检查是否在同一对象层级
        // 例如: jsonPath = "id", fieldPath = "mobile", 都在根对象中
        // 检查jsonPath是否是fieldPath的父路径，或者反之
        int jsonPathLastDot = jsonPath.lastIndexOf('.');
        int fieldPathLastDot = fieldPath.lastIndexOf('.');

        // 如果jsonPath和fieldPath有相同的父路径，说明它们在同一对象层级
        if (jsonPathLastDot > 0 && fieldPathLastDot > 0) {
            String jsonPathParent = jsonPath.substring(0, jsonPathLastDot);
            String fieldPathParent = fieldPath.substring(0, fieldPathLastDot);

            if (jsonPathParent.equals(fieldPathParent)) {
                // 返回jsonPath，表示可以将fieldPath与jsonPath的值关联
                return jsonPath;
            }
        }

        // 如果jsonPath本身就是顶级路径，而fieldPath也是顶级路径
        boolean isJsonPathTopLevel = jsonPath.indexOf('.') < 0;
        boolean isFieldPathTopLevel = fieldPath.indexOf('.') < 0;

        if (isJsonPathTopLevel && isFieldPathTopLevel) {
            return jsonPath;
        }

        // 如果jsonPath是fieldPath的父路径（如 jsonPath="user" fieldPath="user.id"）
        if (fieldPath.startsWith(jsonPath + ".")) {
            return jsonPath;
        }

        // 如果fieldPath是jsonPath的父路径（如 jsonPath="user.id" fieldPath="user"）
        if (jsonPath.startsWith(fieldPath + ".")) {
            return fieldPath;
        }

        return null;
    }

    /**
     处理包含数组路径（包含[*]）的情况
     */
    private static String handleArrayPath(String jsonPath, String fieldPath) {
        String[] jsonPathParts = jsonPath.split("\\.");
        List<String> arrayParts = new ArrayList<>();
        List<String> remainingParts = new ArrayList<>();
        boolean foundFirstArray = false;

        // 找到所有包含[*]的部分和剩余部分
        for (String jsonPathPart : jsonPathParts) {
            if (jsonPathPart.contains("[*]")) {
                arrayParts.add(jsonPathPart);
                foundFirstArray = true;
            } else if (foundFirstArray) {
                // 已经找到第一个数组部分，将剩余部分添加到remainingParts
                remainingParts.add(jsonPathPart);
            }
        }

        if (arrayParts.isEmpty()) {
            // 如果JsonPath中没有[*]，则使用上面的逻辑
            return null;
        }

        return buildResultPath(arrayParts, remainingParts, fieldPath);
    }

    /**
     根据数组部分和剩余部分构建结果路径
     */
    private static String buildResultPath(List<String> arrayParts, List<String> remainingParts, String fieldPath) {
        // 构建结果路径，逐步替换每个[*]为实际索引
        String resultPath = fieldPath;
        String tempFieldPath = fieldPath;

        // 逐个处理每个数组部分
        for (String arrayPartWithWildcard : arrayParts) {
            String arrayPart = arrayPartWithWildcard.replace("[*]", "");

            // 在字段路径中查找对应的数组索引
            int arrayStartIndex = tempFieldPath.indexOf(arrayPart + "[");
            if (arrayStartIndex < 0) {
                return null; // 找不到对应的数组部分，匹配失败
            }

            String arrayIndex = extractArrayIndex(tempFieldPath, arrayPart, arrayStartIndex);
            if (arrayIndex == null) {
                return null; // 索引无效
            }

            // 构建对应的JsonPath路径部分
            String newPath = buildArrayPathPart(tempFieldPath, arrayPart, arrayIndex, arrayStartIndex);

            // 更新结果路径
            if (resultPath.equals(fieldPath)) {
                // 第一次替换
                resultPath = newPath;
            } else {
                // 后续替换，需要找到对应位置进行替换
                String beforeArrayInResult = resultPath.substring(0, resultPath.indexOf(arrayPart + "["));
                resultPath = beforeArrayInResult + arrayPart + "[" + arrayIndex + "]";
            }

            // 更新临时字段路径，移除已处理的部分，用于下一轮查找
            tempFieldPath = updateTempFieldPath(tempFieldPath, tempFieldPath.indexOf("]", arrayStartIndex));
        }

        // 添加剩余的路径部分
        if (!remainingParts.isEmpty()) {
            if (!resultPath.endsWith(".")) {
                resultPath += ".";
            }
            resultPath += String.join(".", remainingParts);
        }

        return resultPath;
    }

    /**
     从字段路径中提取数组索引
     */
    private static String extractArrayIndex(String tempFieldPath, String arrayPart, int arrayStartIndex) {
        if (arrayStartIndex < 0) {
            return null; // 找不到对应的数组部分，匹配失败
        }

        int arrayEndIndex = tempFieldPath.indexOf("]", arrayStartIndex);
        if (arrayEndIndex <= arrayStartIndex) {
            return null; // 无效的数组索引格式
        }

        String arrayIndex = tempFieldPath.substring(arrayStartIndex + arrayPart.length() + 1, arrayEndIndex);

        // 验证是否为数字
        if (!arrayIndex.chars()
                       .allMatch(Character::isDigit)) {
            return null; // 非数字索引，匹配失败
        }

        return arrayIndex;
    }

    /**
     构建数组部分的路径
     */
    private static String buildArrayPathPart(String tempFieldPath, String arrayPart, String arrayIndex,
                                             int arrayStartIndex) {
        String beforeArray = tempFieldPath.substring(0, arrayStartIndex);
        return beforeArray + arrayPart + "[" + arrayIndex + "]";
    }

    /**
     更新临时字段路径
     */
    private static String updateTempFieldPath(String tempFieldPath, int arrayEndIndex) {
        int afterArrayIndex = arrayEndIndex + 1;
        if (afterArrayIndex < tempFieldPath.length()) {
            tempFieldPath = tempFieldPath.substring(afterArrayIndex);
            if (tempFieldPath.startsWith(".")) {
                tempFieldPath = tempFieldPath.substring(1); // 移除开头的点
            }
        } else {
            tempFieldPath = "";
        }
        return tempFieldPath;
    }

    /**
     获取JsonPath路径值及其索引位置
     */
    private static List<PathValueWithIndex> getValuesByJsonPathWithIndex(Object obj, String jsonPath) {
        List<PathValueWithIndex> result = new ArrayList<>();
        if (obj == null || jsonPath == null || jsonPath.trim()
                                                       .isEmpty()) {
            return result;
        }

        // 递归提取值及其索引和路径
        extractValuesWithIndex(obj, jsonPath, result, "", 0);

        return result;
    }

    /**
     递归提取值及其索引和路径
     */
    private static void extractValuesWithIndex(Object obj, String jsonPath, List<PathValueWithIndex> result,
                                               String currentPath, int parentIndex) {
        if (obj == null || jsonPath == null) {
            return;
        }

        // 检查是否有数组标记 [*]
        if (!jsonPath.contains("[*]")) {
            // 如果没有数组标记，直接提取简单值
            Object value = extractSimpleValue(obj, jsonPath);
            if (value != null) {
                String fullPath = currentPath.isEmpty() ? jsonPath : currentPath + "." + jsonPath;
                result.add(new PathValueWithIndex(value, parentIndex, fullPath));
            }
            return;
        }

        // 分割路径
        String[] parts = jsonPath.split("\\.");

        // 找到第一个数组部分
        int firstArrayIndex = findFirstArrayIndex(parts);

        if (firstArrayIndex == -1) {
            // 没有数组部分，提取简单值
            handleSimplePathWithoutArray(jsonPath, obj, result, currentPath, parentIndex);
            return;
        }

        // 提取数组字段名
        String arrayFieldName = parts[firstArrayIndex].replace("[*]", "");

        // 获取数组或集合
        Object arrayOrCollection = extractSimpleValue(obj, arrayFieldName);

        if (arrayOrCollection == null) {
            return;
        }

        // 处理不同的数据类型
        if (arrayOrCollection instanceof Collection<?> collection) {
            handleCollectionTypeWithPath(parts, firstArrayIndex, collection, result, currentPath);
        } else if (arrayOrCollection.getClass()
                                    .isArray()) {
            handleArrayTypeWithPath(parts, firstArrayIndex, arrayOrCollection, result, currentPath);
        } else {
            // 单个对象但路径中包含数组标记
            handleSingleObjectWithArray(parts, firstArrayIndex, arrayOrCollection, result, currentPath, parentIndex);
        }
    }

    /**
     查找第一个数组部分的索引
     */
    private static int findFirstArrayIndex(String[] parts) {
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].contains("[*]")) {
                return i;
            }
        }
        return -1;
    }

    /**
     处理没有数组标记的简单路径
     */
    private static void handleSimplePathWithoutArray(String jsonPath, Object obj, List<PathValueWithIndex> result,
                                                     String currentPath, int parentIndex) {
        Object value = extractSimpleValue(obj, jsonPath.replace(".", ""));
        if (value != null) {
            String fullPath = currentPath.isEmpty() ? jsonPath.replace(".", "") : currentPath + "." + jsonPath.replace(
                    ".", "");
            result.add(new PathValueWithIndex(value, parentIndex, fullPath));
        }
    }

    /**
     处理集合类型
     */
    private static void handleCollectionTypeWithPath(String[] parts, int firstArrayIndex, Collection<?> collection,
                                                     List<PathValueWithIndex> result, String currentPath) {
        if (firstArrayIndex == parts.length - 1) {
            // 如果这是最后一个路径部分，直接添加所有元素及其索引和路径
            handleLastPathElement(collection, result, currentPath, parts[firstArrayIndex].replace("[*]", ""));
        } else {
            // 如果不是最后一个部分，对每个元素递归处理剩余路径
            handleNonLastPathElementForCollection(parts, firstArrayIndex, collection, result, currentPath);
        }
    }

    /**
     处理数组类型
     */
    private static void handleArrayTypeWithPath(String[] parts, int firstArrayIndex, Object arrayOrCollection,
                                                List<PathValueWithIndex> result, String currentPath) {
        int length = Array.getLength(arrayOrCollection);

        if (firstArrayIndex == parts.length - 1) {
            // 如果这是最后一个路径部分，直接添加所有元素及其索引和路径
            handleLastPathElementForArray(arrayOrCollection, length, result, currentPath,
                                          parts[firstArrayIndex].replace("[*]", ""));
        } else {
            // 如果不是最后一个部分，对每个元素递归处理剩余路径
            handleNonLastPathElementForArray(parts, firstArrayIndex, arrayOrCollection, length, result, currentPath);
        }
    }

    /**
     处理单个对象但路径中包含数组标记的情况
     */
    private static void handleSingleObjectWithArray(String[] parts, int firstArrayIndex, Object arrayOrCollection,
                                                    List<PathValueWithIndex> result, String currentPath,
                                                    int parentIndex) {
        // 递归处理剩余路径
        if (firstArrayIndex < parts.length - 1) {
            String remainingPath = String.join(".", Arrays.copyOfRange(parts, firstArrayIndex + 1, parts.length));
            extractValuesWithIndex(arrayOrCollection, remainingPath, result, currentPath, parentIndex);
        }
    }

    /**
     处理最后一个路径元素（集合）
     */
    private static void handleLastPathElement(Collection<?> collection, List<PathValueWithIndex> result,
                                              String currentPath, String arrayFieldName) {
        int index = 0;
        for (Object item : collection) {
            if (item != null) {
                String itemPath = currentPath.isEmpty() ?
                        arrayFieldName + "[" + index + "]" : currentPath + "." + arrayFieldName + "[" + index + "]";
                result.add(new PathValueWithIndex(item, index, itemPath));
            }
            index++;
        }
    }

    /**
     处理非最后一个路径元素（集合）
     */
    private static void handleNonLastPathElementForCollection(String[] parts, int firstArrayIndex,
                                                              Collection<?> collection, List<PathValueWithIndex> result,
                                                              String currentPath) {
        int index = 0;
        String arrayFieldName = parts[firstArrayIndex].replace("[*]", "");
        String remainingPath = String.join(".", Arrays.copyOfRange(parts, firstArrayIndex + 1, parts.length));
        for (Object item : collection) {
            if (item != null) {
                String itemPath = currentPath.isEmpty() ?
                        arrayFieldName + "[" + index + "]" : currentPath + "." + arrayFieldName + "[" + index + "]";
                // 递归处理嵌套对象，保持当前路径
                extractValuesWithIndex(item, remainingPath, result, itemPath, index);
            }
            index++;
        }
    }

    /**
     处理最后一个路径元素（数组）
     */
    private static void handleLastPathElementForArray(Object arrayOrCollection, int length,
                                                      List<PathValueWithIndex> result, String currentPath,
                                                      String arrayFieldName) {
        for (int i = 0; i < length; i++) {
            Object item = Array.get(arrayOrCollection, i);
            if (item != null) {
                String itemPath = currentPath.isEmpty() ?
                        arrayFieldName + "[" + i + "]" : currentPath + "." + arrayFieldName + "[" + i + "]";
                result.add(new PathValueWithIndex(item, i, itemPath));
            }
        }
    }

    /**
     处理非最后一个路径元素（数组）
     */
    private static void handleNonLastPathElementForArray(String[] parts, int firstArrayIndex, Object arrayOrCollection,
                                                         int length, List<PathValueWithIndex> result,
                                                         String currentPath) {
        String arrayFieldName = parts[firstArrayIndex].replace("[*]", "");
        String remainingPath = String.join(".", Arrays.copyOfRange(parts, firstArrayIndex + 1, parts.length));
        for (int i = 0; i < length; i++) {
            Object item = Array.get(arrayOrCollection, i);
            if (item != null) {
                String itemPath = currentPath.isEmpty() ?
                        arrayFieldName + "[" + i + "]" : currentPath + "." + arrayFieldName + "[" + i + "]";
                // 递归处理嵌套对象，保持当前路径
                extractValuesWithIndex(item, remainingPath, result, itemPath, i);
            }
        }
    }

    /**
     递归收集匹配的字段及其路径
     */
    private static void collectMatchedFieldsWithPathInternal(Object obj, Set<String> fieldNames,
                                                             List<FieldWithValueWithPath> result, String currentPath) {
        if (obj == null || fieldNames.isEmpty() || processedObjects.get()
                                                                   .contains(obj)) {
            return;
        }

        processedObjects.get()
                        .add(obj);

        try {
            processObjectByType(obj, fieldNames, result, currentPath);
        } finally {
            processedObjects.get()
                            .remove(obj);
        }
    }

    /**
     根据对象类型处理匹配字段
     */
    private static void processObjectByType(Object obj, Set<String> fieldNames, List<FieldWithValueWithPath> result,
                                            String currentPath) {
        // 处理Map类型
        if (obj instanceof Map<?, ?> map) {
            processMapType(map, fieldNames, result, currentPath);
            return;
        }

        // 处理Collection类型
        if (handleCollectionTypeWithPath(obj, fieldNames, result, currentPath)) {
            return;
        }

        // 处理数组类型
        if (handleArrayTypeWithPath(obj, fieldNames, result, currentPath)) {
            return;
        }

        // 常见基本/包装类型或 Java 核心类，不再深入
        if (isPrimitiveOrWrapper(obj.getClass()) || isSpecialUnmodifiableClass(obj.getClass())) {
            return;
        }

        // 处理普通对象
        handleRegularObjectWithPath(obj, fieldNames, result, currentPath);
    }

    /**
     处理Map类型对象并收集匹配字段
     */
    private static void processMapType(Map<?, ?> map, Set<String> fieldNames, List<FieldWithValueWithPath> result,
                                       String currentPath) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String keyPath = currentPath.isEmpty() ? entry.getKey()
                                                          .toString() : currentPath + "." + entry.getKey();
            // 检查Map的key是否匹配敏感字段名称
            if (fieldNames.contains(entry.getKey()
                                         .toString())) {
                result.add(new FieldWithValueWithPath(null, entry.getValue(), map, keyPath));
            }
            // 递归处理Map的值，无论key是否匹配
            collectMatchedFieldsWithPathInternal(entry.getValue(), fieldNames, result, keyPath);
        }
    }

    /**
     处理集合类型的对象并记录路径
     */
    private static boolean handleCollectionTypeWithPath(Object obj, Set<String> fieldNames,
                                                        List<FieldWithValueWithPath> result, String currentPath) {
        if (obj instanceof Collection<?> collection) {
            int index = 0;
            for (Object item : collection) {
                String itemPath = currentPath + "[" + index + "]";
                collectMatchedFieldsWithPathInternal(item, fieldNames, result, itemPath);
                index++;
            }
            return true;
        }
        return false;
    }

    /**
     处理数组类型的对象并记录路径
     */
    private static boolean handleArrayTypeWithPath(Object obj, Set<String> fieldNames,
                                                   List<FieldWithValueWithPath> result, String currentPath) {
        Class<?> cls = obj.getClass();
        if (cls.isArray()) {
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++) {
                Object element = Array.get(obj, i);
                String itemPath = currentPath + "[" + i + "]";
                collectMatchedFieldsWithPathInternal(element, fieldNames, result, itemPath);
            }
            return true;
        }
        return false;
    }

    /**
     处理普通 Java对象并记录路径
     */
    private static void handleRegularObjectWithPath(Object obj, Set<String> fieldNames,
                                                    List<FieldWithValueWithPath> result, String currentPath) {
        Class<?> clazz = obj.getClass();
        List<Field> fields = getFieldsForClass(clazz, fieldNames);

        // 处理匹配的字段
        processMatchedFieldsWithPath(obj, fields, result, currentPath);

        // 递归处理所有字段值（不仅仅是匹配的字段），以查找嵌套的敏感字段
        // 即使字段名不匹配，也要继续递归查找嵌套对象中的敏感字段
        processNestedFieldsForPath(obj, clazz, fieldNames, result, currentPath);
    }

    /**
     处理匹配的字段并添加到结果中
     */
    private static void processMatchedFieldsWithPath(Object obj, List<Field> fields,
                                                     List<FieldWithValueWithPath> result, String currentPath) {
        for (Field field : fields) {
            try {
                ReflectionUtils.makeAccessible(field);
                Object value = ReflectionUtils.getField(field, obj);
                if (Nones.nonNull(value)) {
                    String fieldPath = currentPath.isEmpty() ? field.getName() : currentPath + "." + field.getName();
                    result.add(new FieldWithValueWithPath(field, value, obj, fieldPath));
                }
            } catch (Exception e) {
                logFieldProcessingError(field, e);
            }
        }
    }

    /**
     递归处理嵌套字段以查找路径
     */
    private static void processNestedFieldsForPath(Object obj, Class<?> clazz, Set<String> fieldNames,
                                                   List<FieldWithValueWithPath> result, String currentPath) {
        ReflectionUtils.doWithFields(clazz, field -> {
            if (shouldSkipField(field)) {
                return;
            }
            try {
                Class<?> fieldType = field.getType();
                if (isSpecialUnmodifiableClass(fieldType) || isPrimitiveOrWrapper(fieldType)) {
                    return;
                }
                ReflectionUtils.makeAccessible(field);
                Object value = ReflectionUtils.getField(field, obj);
                if (Nones.nonNull(value)) {
                    String fieldPath = currentPath.isEmpty() ? field.getName() : currentPath + "." + field.getName();
                    collectMatchedFieldsWithPathInternal(value, fieldNames, result, fieldPath);
                }
            } catch (Exception e) {
                logFieldProcessingError(field, e);
            }
        });
    }

    /**
     提取简单路径的值

     @param obj  源对象
     @param path 简单路径
     @return 提取的值
     */
    private static Object extractSimpleValue(Object obj, String path) {
        if (obj == null || path == null) {
            return null;
        }

        // 如果是Map类型，直接按key获取
        if (obj instanceof Map<?, ?> map) {
            return map.get(path);
        }

        // 如果是普通对象，通过反射获取字段值
        try {
            // 首先尝试直接字段访问
            Field field = ReflectionUtils.findField(obj.getClass(), path);
            if (field != null) {
                ReflectionUtils.makeAccessible(field);
                return ReflectionUtils.getField(field, obj);
            }

            // 如果直接字段访问失败，尝试通过getter方法访问
            var getterMethod = getMethod(obj, path);

            if (getterMethod != null) {
                ReflectionUtils.makeAccessible(getterMethod);
                return getterMethod.invoke(obj);
            }
        } catch (Exception e) {
            log.warn("提取简单路径值时发生异常: {}", e.getMessage());
        }

        return null;
    }

    private static Method getMethod(Object obj, String path) {
        String capitalizedPath = path.substring(0, 1)
                                     .toUpperCase() + path.substring(1);
        Method getterMethod = null;

        // 尝试标准getter方法
        try {
            getterMethod = obj.getClass()
                              .getMethod("get" + capitalizedPath);
        } catch (NoSuchMethodException e) {
            // 尝试is前缀的getter方法（用于boolean类型）
            try {
                getterMethod = obj.getClass()
                                  .getMethod("is" + capitalizedPath);
            } catch (NoSuchMethodException ex) {
                // getter方法不存在，返回null
            }
        }
        return getterMethod;
    }

    /**
     三参数消费者接口，用于接收字段、对象实例和字段值
     */
    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    /**
     四参数消费者接口，用于接收字段、对象实例、字段值和JsonPath对应的值
     */
    @FunctionalInterface
    public interface QuadConsumer<T, U, V, W> {
        /**
         执行操作

         @param field      字段对象
         @param instance   字段所属的对象实例
         @param value      字段的值
         @param jsonValues JsonPath对应的值
         */
        void accept(T field, U instance, V value, W jsonValues);
    }

    /**
     存储路径值及其索引
     */
    record PathValueWithIndex(Object value, Integer index, String path) {
    }

    /**
     存储字段、值和路径
     */
    record FieldWithValueWithPath(Field field, Object value, Object instance, String path) {
    }

    /**
     存储字段、值、路径和JsonPath关联值
     */
    record FieldWithValueWithPathAndMatch(Field field, Object value, Object instance, String fieldPath,
                                          List<Object> jsonPathValues) {
    }
}