package top.cjf_rb.core.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.cjf_rb.core.context.type.Ciphertext;

import java.io.IOException;
import java.time.*;
import java.util.Date;


/**
 * AppWebConfig 类用于配置 Spring MVC 的 Web 应用程序。
 * 该类实现了 WebMvcConfigurer 接口，并提供了多个 Bean 配置方法。
 * 核心功能包括：
 * 1. 配置跨域资源共享（CORS）。
 * 2. 配置内容协商，默认使用 JSON 格式。
 * 3. 配置 Jackson 序列化和反序列化工具，处理特定类型的序列化和反序列化。
 * 4. 配置全局类型处理，包括日期和时间的转换。
 * <p>
 * 使用示例：
 * 在 Spring Boot 应用程序中，将此配置类添加到配置类列表中，以便应用这些配置。
 *
 * @Configuration public class AppConfig {
 * @Bean public AppWebConfig appWebConfig() {
 * return new AppWebConfig();
 * }
 * }
 * <p>
 * 构造函数参数：
 * 该类没有构造函数，因此不需要传递任何参数。
 * <p>
 * 使用限制或潜在的副作用：
 * 1. 配置了全局的 CORS 策略，可能会影响所有请求的跨域资源共享。
 * 2. 配置了默认的 JSON 内容协商策略，可能会影响非 JSON 请求的处理。
 * 3. 自定义的 Jackson 序列化和反序列化器可能会影响特定类型的 JSON 转换。
 */
@Configuration
public class AppWebConfig implements WebMvcConfigurer {

    /**
     * 配置cors跨域共享
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedMethods(CorsConfiguration.ALL)
                .allowedHeaders(CorsConfiguration.ALL)
                .allowedOriginPatterns(CorsConfiguration.ALL)
                .maxAge(3600);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * 默认的jackson序列化工具
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
        // Long类型转字符串, 主要是因为JavaScript的Long类型精度比Java低的问题
        return jackson2ObjectMapperBuilder.serializerByType(Long.class, new JsonSerializer<Long>() {
                                              @Override
                                              public void serialize(Long value, JsonGenerator gen,
                                                                    SerializerProvider serializers) throws IOException {
                                                  String longValue = value.toString();
                                                  gen.writeString(longValue);
                                              }
                                          })
                                          .deserializerByType(Long.class, new JsonDeserializer<Long>() {
                                              @Override
                                              public Long deserialize(JsonParser p,
                                                                      DeserializationContext deserializationContext) throws IOException {
                                                  return Long.parseLong(p.getText()
                                                                         .trim());
                                              }
                                          })
                                          .build();
    }

    /**
     * 配置全局类型处理
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jsonBuilder -> jsonBuilder.serializerByType(Instant.class, new JsonSerializer<Instant>() {
                                             @Override
                                             public void serialize(Instant value, JsonGenerator gen,
                                                                   SerializerProvider serializers) throws IOException {
                                                 gen.writeNumber(value.toEpochMilli());
                                             }
                                         })
                                         .serializerByType(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                                             @Override
                                             public void serialize(LocalDateTime value, JsonGenerator gen,
                                                                   SerializerProvider serializers) throws IOException {
                                                 Instant instant = value.atZone(ZoneId.systemDefault())
                                                                        .toInstant();
                                                 gen.writeNumber(instant.toEpochMilli());
                                             }
                                         })
                                         .serializerByType(LocalDate.class, new JsonSerializer<LocalDate>() {
                                             @Override
                                             public void serialize(LocalDate value, JsonGenerator gen,
                                                                   SerializerProvider serializers) throws IOException {
                                                 Instant instant = value.atStartOfDay(ZoneId.systemDefault())
                                                                        .toInstant();
                                                 gen.writeNumber(instant.toEpochMilli());
                                             }
                                         })
                                         .serializerByType(LocalTime.class, new JsonSerializer<LocalTime>() {
                                             @Override
                                             public void serialize(LocalTime value, JsonGenerator gen,
                                                                   SerializerProvider serializers) throws IOException {
                                                 LocalDateTime localDateTime = LocalDate.now()
                                                                                        .atTime(value);
                                                 Instant instant = localDateTime.atZone(ZoneId.systemDefault())
                                                                                .toInstant();
                                                 gen.writeNumber(instant.toEpochMilli());
                                             }
                                         })
                                         .serializerByType(YearMonth.class, new JsonSerializer<YearMonth>() {
                                             @Override
                                             public void serialize(YearMonth value, JsonGenerator gen,
                                                                   SerializerProvider serializers) throws IOException {
                                                 gen.writeString(value.toString());
                                             }
                                         })
                                         .serializerByType(Ciphertext.class, new JsonSerializer<Ciphertext>() {
                                             @Override
                                             public void serialize(Ciphertext value, JsonGenerator gen,
                                                                   SerializerProvider serializerProvider) throws IOException {
                                                 gen.writeString(value.getValue());
                                             }
                                         })
                                         .deserializerByType(Instant.class, new JsonDeserializer<Instant>() {
                                             @Override
                                             public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                                                 String value = p.getText()
                                                                 .trim();
                                                 return Instant.ofEpochMilli(Long.parseLong(value));
                                             }
                                         })
                                         .deserializerByType(LocalDateTime.class,
                                                             new JsonDeserializer<LocalDateTime>() {
                                                                 @Override
                                                                 public LocalDateTime deserialize(JsonParser p,
                                                                                                  DeserializationContext ctxt) throws IOException {
                                                                     String value = p.getText()
                                                                                     .trim();
                                                                     return LocalDateTime.ofInstant(
                                                                             Instant.ofEpochMilli(
                                                                                     Long.parseLong(value)),
                                                                             ZoneOffset.systemDefault());
                                                                 }
                                                             })
                                         .deserializerByType(LocalDate.class, new JsonDeserializer<LocalDate>() {
                                             @Override
                                             public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                                                 String value = p.getText()
                                                                 .trim(); return LocalDate.ofInstant(
                                                         Instant.ofEpochMilli(Long.parseLong(value)),
                                                         ZoneOffset.systemDefault());
                                             }
                                         })
                                         .deserializerByType(LocalTime.class, new JsonDeserializer<LocalTime>() {
                                             @Override
                                             public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                                                 String value = p.getText()
                                                                 .trim(); return LocalTime.ofInstant(
                                                         Instant.ofEpochMilli(Long.parseLong(value)),
                                                         ZoneOffset.systemDefault());
                                             }
                                         })
                                         .deserializerByType(Ciphertext.class, new JsonDeserializer<Ciphertext>() {
                                             @Override
                                             public Ciphertext deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                                                 return new Ciphertext(p.getText()
                                                                        .trim());
                                             }
                                         });
    }

    @Bean
    public Converter<String, Date> string2DateConverter() {
        return new Converter<String, Date>() {
            @Override
            public Date convert(String source) {
                return new Date(Long.parseLong(source));
            }
        };
    }

    @Bean
    public Converter<String, Instant> string2InstantConverter() {
        return new Converter<String, Instant>() {
            @Override
            public Instant convert(String source) {
                return Instant.ofEpochMilli(Long.parseLong(source));
            }
        };
    }

    @Bean
    public Converter<String, LocalDateTime> string2LocalDateTimeConverter() {
        return new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(source)),
                                               ZoneOffset.systemDefault());
            }
        };
    }

    @Bean
    public Converter<String, LocalDate> string2LocalDateConverter() {
        return new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                return LocalDate.ofInstant(Instant.ofEpochMilli(Long.parseLong(source)), ZoneOffset.systemDefault());
            }
        };
    }

    @Bean
    public Converter<String, LocalTime> string2LocalTimeConverter() {
        return new Converter<String, LocalTime>() {
            @Override
            public LocalTime convert(String source) {
                return LocalTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(source)), ZoneOffset.systemDefault());
            }
        };
    }

}
