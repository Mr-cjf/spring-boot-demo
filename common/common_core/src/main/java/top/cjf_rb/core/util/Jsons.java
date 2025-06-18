package top.cjf_rb.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import top.cjf_rb.core.constant.AppSystemConst;
import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.exception.AppException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * <pre>
 * 提供Jackson常用序列化和反序列化功能。并设置统一转换格式。
 *
 * 1. 使用默认时区
 * 2. 值为null，不进行序列化
 * 3. 默认静态方法使用系统默认的ObjectMapper进行序列化和反序列化
 * 4. withTimeFormat()方法序列化时间类型(Date、Instant、LocalDate、LocalDateTime)时，格式统一为：yyyy-MM-dd HH:mm:ss
 * 5.在反序列化过程中，忽略不存在的属性字段
 * </pre>
 */
@Component
public final class Jsons implements ApplicationContextAware {
    private static final JsonOps TIME_FORMAT_JSONOPS = new JsonOps();
    private static JsonOps defaultJsonOps;

    /**
     * 解析json字符串
     */
    public static JsonNode parse(@NonNull String json) {
        return defaultJsonOps.parse(json);
    }

    /**
     * 解析json字符串
     */
    public static <T> T parse(@NonNull String json, @NonNull Class<T> clazz) {
        return defaultJsonOps.parse(json, clazz);
    }

    /**
     * 解析json字符串
     */
    public static <T> T parse(@NonNull String json, @NonNull TypeReference<T> typeReference) {
        return defaultJsonOps.parse(json, typeReference);
    }

    /**
     * 解析json字符串
     */
    public static <T> T parse(@NonNull String json, @NonNull JavaType javaType) {
        return defaultJsonOps.parse(json, javaType);
    }

    /**
     * 对象转换
     */
    public static <T> T convert(@NonNull Object object, @NonNull Class<T> clazz) {
        return defaultJsonOps.convert(object, clazz);
    }

    /**
     * 复杂对象转换 -> TypeReference<Set<Users>> ref = new TypeReference<>() {};
     */
    public static <T> T convert(@NonNull Object object, @NonNull TypeReference<T> typeReference) {
        return defaultJsonOps.convert(object, typeReference);
    }

    /**
     * json序列化
     */
    public static String stringify(@NonNull Object object) {
        return defaultJsonOps.stringify(object);
    }

    /**
     * 获取时间格式的Json操作
     */
    public static JsonOps withTimeFormat() {
        return TIME_FORMAT_JSONOPS;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Jackson2ObjectMapperBuilder objectMapperBuilder = applicationContext.getBean(Jackson2ObjectMapperBuilder.class);
        // 初始化 jsonOptions
        if (Objects.isNull(defaultJsonOps)) {
            defaultJsonOps = new JsonOps(objectMapperBuilder.build());
        }
    }

    public static class JsonOps {
        private final ObjectMapper objectMapper;

        private JsonOps() {
            // 初始化 ObjectMapper
            this.objectMapper = this.initObjectMapper();
        }

        private JsonOps(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        private ObjectMapper initObjectMapper() {
            DateTimeFormatter dateFormatter = DateTimes.DATE_FORMATTER;
            DateTimeFormatter datetimeFormatter = DateTimes.DATETIME_FORMATTER;

            // 初始化 ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();

            // 去掉默认的时间戳格式
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            // 序列化时，日期的统一格式
            objectMapper.setDateFormat(new SimpleDateFormat(AppSystemConst.DATETIME_FORMAT));

            // 空值不序列化
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            // 序列化时，对象为空时是否报错
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            // 反序列化时, 忽略不存在的字段
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            // 枚举序列化和反序列化
            objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true); objectMapper.configure(
                    DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);

            // Java 8+ 时间系列序列化和反序列化模块，继承自jsr310
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            // LocalDateTime 序列化
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(datetimeFormatter));
            // LocalDate 序列化
            javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
            // Instant 序列化
            javaTimeModule.addSerializer(Instant.class, new InstantSerializer(InstantSerializer.INSTANCE, false, false,
                                                                              datetimeFormatter) {
            });
            // LocalDateTime 反序列化
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(datetimeFormatter));
            // LocalDate 反序列化
            javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
            // Instant 反序列化
            javaTimeModule.addDeserializer(Instant.class,
                                           new InstantDeserializer<>(InstantDeserializer.INSTANT, datetimeFormatter) {
                                           });

            objectMapper.registerModule(javaTimeModule);

            return objectMapper;
        }

        /**
         * 解析json字符串
         */
        public JsonNode parse(String json) {
            try {
                return objectMapper.readTree(json);
            } catch (IOException e) {
                throw new AppException(ErrorCodeEnum.DATA_ERRORS, "Json解析异常", e);
            }
        }

        /**
         * 解析json字符串
         */
        public <T> T parse(String json, Class<T> clazz) {
            try {
                return objectMapper.readValue(json, clazz);
            } catch (IOException e) {
                throw new AppException(ErrorCodeEnum.DATA_ERRORS, "Json解析异常", e);
            }
        }

        /**
         * 解析json字符串
         */
        public <T> T parse(String json, JavaType javaType) {
            try {
                return objectMapper.readValue(json, javaType);
            } catch (IOException e) {
                throw new AppException(ErrorCodeEnum.DATA_ERRORS, "Json解析异常", e);
            }
        }

        /**
         * 解析json字符串
         */
        public <T> T parse(String json, TypeReference<T> typeReference) {
            try {
                return objectMapper.readValue(json, typeReference);
            } catch (IOException e) {
                throw new AppException(ErrorCodeEnum.DATA_ERRORS, "Json解析异常", e);
            }
        }

        /**
         * 对象转换
         */
        public <T> T convert(@NonNull Object object, @NonNull Class<T> clazz) {
            return objectMapper.convertValue(object, clazz);
        }

        /**
         * 复杂对象转换 -> TypeReference<Set<Users>> ref = new TypeReference<>() {};
         */
        public <T> T convert(@NonNull Object object, @NonNull TypeReference<T> typeReference) {
            return objectMapper.convertValue(object, typeReference);
        }

        /**
         * 对象转换
         */
        public <T> T convert(@NonNull Object object, @NonNull JavaType javaType) {
            return objectMapper.convertValue(object, javaType);
        }

        /**
         * json序列化
         */
        public String stringify(@NonNull Object object) {
            try {
                return objectMapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                throw new AppException(ErrorCodeEnum.DATA_ERRORS, "Json序列化异常", e);
            }
        }
    }
}
