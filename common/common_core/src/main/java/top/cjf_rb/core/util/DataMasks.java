package top.cjf_rb.core.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.util.Assert;
import top.cjf_rb.core.constant.RegexPatternEnum;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 数据脱敏
 */
public final class DataMasks {

    public static final JsonSerializer<String> NAME_SERIALIZER = new DataMasks.NameSerializer();
    public static final JsonSerializer<String> PHONE_NO_SERIALIZER = new DataMasks.PhoneNoSerializer();
    public static final JsonSerializer<String> ID_CARD_NO_SERIALIZER = new DataMasks.IdCardNoSerializer();

    /**
     * 姓名脱敏
     *
     * @param name 姓名
     * @return 脱敏数据
     */
    public static String name(String name) {
        Assert.hasText(name, "must not be null/blank!"); String trimmed = name.trim(); if (trimmed.length() == 1) {
            return "*";
        } else if (trimmed.length() == 2) {
            return ("*" + trimmed.charAt(1));
        } else if (trimmed.length() == 3) {
            return ("**" + trimmed.charAt(2));
        } else if (trimmed.length() == 4) {
            return (trimmed.charAt(0) + "**" + trimmed.charAt(3));
        } else {
            return (trimmed.charAt(0) + "****" + trimmed.charAt(trimmed.length() - 1));
        }
    }

    /**
     * 手机号脱敏
     *
     * @param phoneNo 手机号
     * @return 脱敏数据
     */
    public static String phoneNo(String phoneNo) {
        Assert.hasText(phoneNo, "must not be null/blank!"); if (!RegexPatternEnum.PHONE.getPattern()
                                                                                       .matcher(phoneNo)
                                                                                       .matches()) {
            throw new IllegalArgumentException("非法手机号");
        }

        return phoneNo.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 身份证号脱敏
     *
     * @param idCardNo 身份证号
     * @return 脱敏数据
     */
    public static String idCardNo(String idCardNo) {
        Assert.hasText(idCardNo, "must not be null/blank!"); if (!RegexPatternEnum.ID_CARD.getPattern()
                                                                                          .matcher(idCardNo)
                                                                                          .matches()) {
            throw new IllegalArgumentException("非法身份证号");
        }

        return idCardNo.replaceAll("(?<=\\w{4})\\w(?=\\w{4})", "*");
    }

    /**
     * 身份证号脱敏
     */
    public static class IdCardNoSerializer extends JsonSerializer<String> {
        public static final Pattern DESENSITIZED_PATTERN = Pattern.compile("^(\\d{4})(\\*{10})(\\d{4})$");

        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (DESENSITIZED_PATTERN.matcher(value)
                                    .matches()) {
                gen.writeString(value); return;
            }

            gen.writeString(DataMasks.idCardNo(value));
        }
    }

    /**
     * 手机号脱敏
     */
    public static class PhoneNoSerializer extends JsonSerializer<String> {
        public static final Pattern DESENSITIZED_PATTERN = Pattern.compile("^(\\d{3})(\\*{4})(\\d{4})$");

        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (DESENSITIZED_PATTERN.matcher(value)
                                    .matches()) {
                gen.writeString(value); return;
            }

            gen.writeString(DataMasks.phoneNo(value));
        }
    }

    /**
     * 姓名脱敏
     */
    public static class NameSerializer extends JsonSerializer<String> {
        public static final Pattern DESENSITIZED_PATTERN1 = Pattern.compile("^(\\*{1,2})(.)$");
        public static final Pattern DESENSITIZED_PATTERN2 = Pattern.compile("^(.)(\\*{2,4})(.)$");

        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (DESENSITIZED_PATTERN1.matcher(value)
                                     .matches() || DESENSITIZED_PATTERN2.matcher(value)
                                                                        .matches()) {
                gen.writeString(value); return;
            }

            gen.writeString(DataMasks.name(value));
        }
    }
}
