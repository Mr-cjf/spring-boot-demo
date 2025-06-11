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
 * @author lty
 */
@Configuration
public class AppWebConfig implements WebMvcConfigurer {

    /**
     * 配置cors跨域共享
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowCredentials(true).allowedMethods(CorsConfiguration.ALL)
                .allowedHeaders(CorsConfiguration.ALL).allowedOriginPatterns(CorsConfiguration.ALL).maxAge(3600);
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
            public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                String longValue = value.toString();
                gen.writeString(longValue);
            }
        }).deserializerByType(Long.class, new JsonDeserializer<Long>() {
            @Override
            public Long deserialize(JsonParser p, DeserializationContext deserializationContext) throws IOException {
                return Long.parseLong(p.getText().trim());
            }
        }).build();
    }

    /**
     * 配置全局类型处理
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return jsonBuilder -> jsonBuilder.serializerByType(Instant.class, new JsonSerializer<Instant>() {
            @Override
            public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeNumber(value.toEpochMilli());
            }
        }).serializerByType(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                Instant instant = value.atZone(ZoneId.systemDefault()).toInstant();
                gen.writeNumber(instant.toEpochMilli());
            }
        }).serializerByType(LocalDate.class, new JsonSerializer<LocalDate>() {
            @Override
            public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                Instant instant = value.atStartOfDay(ZoneId.systemDefault()).toInstant();
                gen.writeNumber(instant.toEpochMilli());
            }
        }).serializerByType(LocalTime.class, new JsonSerializer<LocalTime>() {
            @Override
            public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                LocalDateTime localDateTime = LocalDate.now().atTime(value);
                Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                gen.writeNumber(instant.toEpochMilli());
            }
        }).serializerByType(YearMonth.class, new JsonSerializer<YearMonth>() {
            @Override
            public void serialize(YearMonth value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                gen.writeString(value.toString());
            }
        }).serializerByType(Ciphertext.class, new JsonSerializer<Ciphertext>() {
            @Override
            public void serialize(Ciphertext value, JsonGenerator gen, SerializerProvider serializerProvider)
                    throws IOException {
                gen.writeString(value.getValue());
            }
        }).deserializerByType(Instant.class, new JsonDeserializer<Instant>() {
            @Override
            public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getText().trim();
                return Instant.ofEpochMilli(Long.parseLong(value));
            }
        }).deserializerByType(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getText().trim();
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(value)), ZoneOffset.systemDefault());
            }
        }).deserializerByType(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getText().trim();
                return LocalDate.ofInstant(Instant.ofEpochMilli(Long.parseLong(value)), ZoneOffset.systemDefault());
            }
        }).deserializerByType(LocalTime.class, new JsonDeserializer<LocalTime>() {
            @Override
            public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String value = p.getText().trim();
                return LocalTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(value)), ZoneOffset.systemDefault());
            }
        }).deserializerByType(Ciphertext.class, new JsonDeserializer<Ciphertext>() {
            @Override
            public Ciphertext deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return new Ciphertext(p.getText().trim());
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
