package top.cjf_rb.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.FileSystems;

/**
 * 分隔符枚举类
 */
@AllArgsConstructor
@Getter
public enum AppSeparatorConst {

    /**
     * 无分隔符
     */
    NO(""),

    /**
     * 冒号分隔符
     */
    COLON(":"),

    /**
     * 空格分隔符
     */
    SPACE(" "),

    /**
     * 下划线分隔符
     */
    UNDERLINE("_"),

    /**
     * 连字符/减号/中横线分隔符
     */
    HYPHEN("-"),

    /**
     * 栏杆分隔符
     */
    BAR("|"),

    /**
     * 句号分隔符
     */
    PERIOD("."),

    /**
     * 逗号分隔符
     */
    COMMA(","),

    /**
     * 等号分隔符
     */
    EQUAL("="),

    /**
     * and分隔符
     */
    AMPERSAND("&"),

    /**
     * 井号分隔符
     */
    POUND("#"),

    /**
     * 系统换行符
     */
    LINE(System.lineSeparator()),

    /**
     * 文件路径分隔符
     */
    FILE(FileSystems.getDefault().getSeparator());

    private final String separator;


}