package top.cjf_rb.core.util;

import top.cjf_rb.core.constant.ErrorCodeEnum;
import top.cjf_rb.core.constant.RegexPatternEnum;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * 身份证号校工具
 *
 * @author lty
 */
public class IdCards {
    public static final Pattern PATTERN = RegexPatternEnum.ID_CARD.getPattern();
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
    private static final int[] COEFFICIENT = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] CHECK_CODE = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    private static final Set<Integer> AREA_CODE;

    static {
        AREA_CODE = new HashSet<>(34);
        AREA_CODE.add(11); // 北京市
        AREA_CODE.add(12); // 天津市
        AREA_CODE.add(13); // 河北省
        AREA_CODE.add(14); // 山西省
        AREA_CODE.add(15); // 内蒙古自治区
        AREA_CODE.add(21); // 辽宁省
        AREA_CODE.add(22); // 吉林省
        AREA_CODE.add(23); // 黑龙江省
        AREA_CODE.add(31); // 上海市
        AREA_CODE.add(32); // 江苏省
        AREA_CODE.add(33); // 浙江省
        AREA_CODE.add(34); // 安徽省
        AREA_CODE.add(35); // 福建省
        AREA_CODE.add(36); // 江西省
        AREA_CODE.add(37); // 山东省
        AREA_CODE.add(41); // 河南省
        AREA_CODE.add(42); // 湖北省
        AREA_CODE.add(43); // 湖南省
        AREA_CODE.add(44); // 广东省
        AREA_CODE.add(45); // 广西壮族自治区
        AREA_CODE.add(46); // 海南省
        AREA_CODE.add(50); // 重庆市
        AREA_CODE.add(51); // 四川省
        AREA_CODE.add(52); // 贵州省
        AREA_CODE.add(53); // 云南省
        AREA_CODE.add(54); // 西藏自治区
        AREA_CODE.add(61); // 陕西省
        AREA_CODE.add(62); // 甘肃省
        AREA_CODE.add(63); // 青海省
        AREA_CODE.add(64); // 宁夏回族自治区
        AREA_CODE.add(65); // 新疆维吾尔自治区
        AREA_CODE.add(71); // 台湾省
        AREA_CODE.add(81); // 香港特别行政区
        AREA_CODE.add(82); // 澳门特别行政区
    }

    private IdCards() {
    }

    /**
     * 校验身份证号
     *
     * @param idCard 18位身份证号
     * @return true-身份证号正确 false-身份证号错误
     */
    public static boolean validate(String idCard) {
        Asserts.notNull(idCard, ErrorCodeEnum.PARAMS_INCORRECT, "身份证号不能为空");

        if (PATTERN.matcher(idCard).matches()) {
            return validateCheckCode(idCard) && validateAreaCode(idCard) && validateDate(idCard);
        }

        return false;
    }

    /**
     * 校验校验码
     *
     * @param idCard 身份证号
     * @return true-校验码正确 false-校验码错误
     */
    private static boolean validateCheckCode(String idCard) {
        var idCardCharArray = idCard.toCharArray();
        var sum = 0;
        for (int i = 0; i < COEFFICIENT.length; i++) {
            var c = idCardCharArray[i];
            if (c >= 48 && c <= 57) {
                sum += COEFFICIENT[i] * (c - 48);
            } else {
                return false;
            }
        }

        return CHECK_CODE[sum % 11] == idCardCharArray[17];
    }

    /**
     * 校验地区编码
     *
     * @param idCard 身份证号
     * @return true-地区编码正确 false-地区编码错误
     */
    private static boolean validateAreaCode(String idCard) {
        return AREA_CODE.contains(Integer.valueOf(idCard.substring(0, 2)));
    }

    /**
     * 校验日期
     *
     * @param idCard 身份证号
     * @return true-日期正确 false-日期错误
     */
    private static boolean validateDate(String idCard) {
        String birthday = idCard.substring(6, 14);
        try {
            // 尝试将字符串解析为LocalDate
            LocalDate.parse(birthday, formatter);
            return true;
        } catch (Exception e) {
            // 解析失败，说明不是有效的日期
            return false;
        }

    }

    public static void main(String[] args) {
        String idCardDate = ""; // 示例日期字符串
        if (validate(idCardDate)) {
            System.out.println(idCardDate + " 是一个有效的日期。");
        } else {
            System.out.println(idCardDate + " 不是一个有效的日期。");
        }
    }
}

