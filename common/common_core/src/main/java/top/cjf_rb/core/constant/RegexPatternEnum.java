package top.cjf_rb.core.constant;

import lombok.Getter;

import java.util.regex.Pattern;

/**
 * 系统常用正则
 */
@Getter
public enum RegexPatternEnum {

    /**
     * 手机号码正则
     */
    PHONE("^1\\d{10}$"),

    /**
     * 身份证号正则: 身份证号(18位数字)，最后一位是校验位，可能为数字或字符X
     */
    ID_CARD("^([1-9]\\d{5})(\\d{8})((\\d{3})(\\d|x|X))$"),

    /**
     * 账号正则: 以字母开头，长度在6~16之间，只能包含字母、数字和下划线
     */
    ACCOUNT("^[a-zA-Z]\\w{5,15}$"),

    /**
     * 密码正则: 必须包含大小写写字母和数字，不能使用特殊字符(下划线除外)，长度在8~16之间
     */
    PASSWORD("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])\\w{8,16}$");

    private final String regex;
    private final Pattern pattern;

    RegexPatternEnum(String regex) {
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
    }
}