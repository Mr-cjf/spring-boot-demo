package top.cjf_rb.core.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 港澳台居民证件类型枚举类
 <p>
 使用示例：
 <p>
 // 根据code获取枚举
 IdCardTypeEnum idCardType = IdCardTypeEnum.from(0); // RESIDENTS_PASSPORT_HK_MACAU
 <p>
 // 获取描述
 String description = idCardType.getDescription(); // 港澳居民来往内地通行证
 <p>
 // 根据描述获取枚举
 IdCardTypeEnum idCardType2 = IdCardTypeEnum.fromDescription("港澳居民来往内地通行证");
 */
@Getter
@AllArgsConstructor
public enum IdCardTypeEnum {
    /**
     居民身份证（18位身份证）
     */
    ID_CARD(0, "身份证"),

    /**
     港澳居民来往内地通行证（回乡证）
     */
    RESIDENTS_PASSPORT_HK_MACAU(1, "港澳居民来往内地通行证"),

    /**
     台湾居民来往大陆通行证（台胞证）
     */
    RESIDENTS_PASSPORT_TAIWAN(2, "台湾居民来往大陆通行证"),


    /**
     港澳居民身份证
     */
    HK_MACAU_ID_CARD(11, "港澳居民身份证"),

    /**
     台湾居民身份证
     */
    TAIWAN_ID_CARD(12, "台湾居民身份证"),

    /**
     其他证件
     */
    OTHER(13, "其他");

    @EnumValue
    private final Integer code;
    private final String description;

    /**
     根据code获取枚举
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static IdCardTypeEnum from(Object code) {
        if (code instanceof Integer item) {
            return findByCondition(type -> type.code.equals(item));
        } else if (code instanceof String item) {
            return findByCondition(type -> type.name()
                                               .equals(item));
        }
        return null;
    }

    /**
     根据条件查找枚举值
     */
    private static IdCardTypeEnum findByCondition(Predicate<IdCardTypeEnum> condition) {
        return Arrays.stream(values())
                     .filter(condition)
                     .findFirst()
                     .orElse(null);
    }

    /**
     判断证件类型是否包含出生日期信息

     @return true表示包含出生日期信息，false表示不包含
     */
    public boolean hasBirthDate() {
        // 身份证、出生医学证明、户口簿等包含出生日期信息
        return this == ID_CARD || this == RESIDENTS_PASSPORT_HK_MACAU || this == RESIDENTS_PASSPORT_TAIWAN ||
                this == HK_MACAU_ID_CARD || this == TAIWAN_ID_CARD;
    }

    /**
     判断证件类型是否包含性别信息

     @return true表示包含性别信息，false表示不包含
     */
    public boolean hasGender() {
        // 身份证、出生医学证明、户口簿等包含性别信息
        return this == ID_CARD || this == RESIDENTS_PASSPORT_HK_MACAU || this == RESIDENTS_PASSPORT_TAIWAN ||
                this == HK_MACAU_ID_CARD || this == TAIWAN_ID_CARD;
    }
}
