package top.cjf_rb.core.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.cjf_rb.core.constant.IdCardTypeEnum;
import top.cjf_rb.core.constant.SexEnum;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 证件号码工具类
 用于根据证件类型和证件号码识别出生日期和性别

 使用示例：

 // 提取身份证中的出生日期
 LocalDate birthDate = IdCardUtils.extractBirthDate(IdCardTypeEnum.ID_CARD, "110101199003072517");

 // 提取身份证中的性别
 SexEnum gender = IdCardUtils.extractGender(IdCardTypeEnum.ID_CARD, "110101199003072517");

 // 验证身份证格式
 boolean isValid = IdCardUtils.validateIdCard(IdCardTypeEnum.ID_CARD, "110101199003072517");
 */
@Slf4j
@Component
public class IdCardUtils {

    /**
     中国公民一代身份证号码长度。
     */
    private static final int CHINA_ID_MIN_LENGTH = 15;

    /**
     中国公民二代身份证号码长度。
     */
    private static final int CHINA_ID_MAX_LENGTH = 18;

    /**
     每位加权因子
     */
    private static final int[] POWER = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     最低年限
     */
    private static final int MIN = 1930;
    // 18位身份证号码的正则表达式
    private static final Pattern ID_CARD_18_PATTERN = Pattern.compile(
            "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");
    // 15位身份证号码的正则表达式
    private static final Pattern ID_CARD_15_PATTERN = Pattern.compile(
            "^[1-9]\\d{5}\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}$");
    // 港澳居民来往内地通行证的正则表达式（旧版和新版）
    private static final Pattern HK_MACAU_PASS_PATTERN = Pattern.compile("^[HM]\\d{8}(\\(\\d\\))?$|^\\d{8}[HM]$");
    // 台湾居民来往大陆通行证的正则表达式
    private static final Pattern TAIWAN_PASS_PATTERN = Pattern.compile("^\\d{8}$|^\\d{10}$");
    private static final Map<String, String> CITY_CODES = new HashMap<>();

    static {
        CITY_CODES.put("11", "北京");
        CITY_CODES.put("12", "天津");
        CITY_CODES.put("13", "河北");
        CITY_CODES.put("14", "山西");
        CITY_CODES.put("15", "内蒙古");
        CITY_CODES.put("21", "辽宁");
        CITY_CODES.put("22", "吉林");
        CITY_CODES.put("23", "黑龙江");
        CITY_CODES.put("31", "上海");
        CITY_CODES.put("32", "江苏");
        CITY_CODES.put("33", "浙江");
        CITY_CODES.put("34", "安徽");
        CITY_CODES.put("35", "福建");
        CITY_CODES.put("36", "江西");
        CITY_CODES.put("37", "山东");
        CITY_CODES.put("41", "河南");
        CITY_CODES.put("42", "湖北");
        CITY_CODES.put("43", "湖南");
        CITY_CODES.put("44", "广东");
        CITY_CODES.put("45", "广西");
        CITY_CODES.put("46", "海南");
        CITY_CODES.put("50", "重庆");
        CITY_CODES.put("51", "四川");
        CITY_CODES.put("52", "贵州");
        CITY_CODES.put("53", "云南");
        CITY_CODES.put("54", "西藏");
        CITY_CODES.put("61", "陕西");
        CITY_CODES.put("62", "甘肃");
        CITY_CODES.put("63", "青海");
        CITY_CODES.put("64", "宁夏");
        CITY_CODES.put("65", "新疆");
        CITY_CODES.put("71", "台湾");
        CITY_CODES.put("81", "香港");
        CITY_CODES.put("82", "澳门");
        CITY_CODES.put("91", "国外");
    }

    /**
     验证身份证是否合法

     @param idCard 身份证号码
     @return 是否合法
     */
    public static boolean validateCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return false;
        }
        idCard = idCard.trim();
        if (idCard.length() == CHINA_ID_MAX_LENGTH) {
            return validateIdCard18(idCard);
        } else if (idCard.length() == CHINA_ID_MIN_LENGTH) {
            return validateIdCard15(idCard);
        }
        return false;
    }

    /**
     验证18位身份证是否合法

     @param idCard 身份证号码
     @return 是否合法
     */
    public static boolean validateIdCard18(String idCard) {
        boolean bTrue = false;
        if (idCard.length() == CHINA_ID_MAX_LENGTH) {
            // 前17位
            String code17 = idCard.substring(0, 17);
            // 第18位
            String code18 = idCard.substring(17, CHINA_ID_MAX_LENGTH);
            if (isNum(code17)) {
                char[] cArr = code17.toCharArray();
                int[] iCard = convertCharToInt(cArr);
                int iSum17 = getPowerSum(iCard);
                // 获取校验位
                String val = getCheckCode18(iSum17);
                if (!val.isEmpty()) {
                    if (val.equalsIgnoreCase(code18)) {
                        bTrue = true;
                    }
                }
            }
        }
        return bTrue;
    }

    /**
     验证15位身份证是否合法

     @param idCard 身份证号码
     @return 是否合法
     */
    public static boolean validateIdCard15(String idCard) {
        if (idCard.length() != CHINA_ID_MIN_LENGTH) {
            return false;
        }
        if (isNum(idCard)) {
            String proCode = idCard.substring(0, 2);
            if (CITY_CODES.get(proCode) == null) {
                return false;
            }
            String birthCode = idCard.substring(6, 12);
            int year = Integer.parseInt(birthCode.substring(0, 2)) + 1900;
            int month = Integer.parseInt(birthCode.substring(2, 4));
            int day = Integer.parseInt(birthCode.substring(4, 6));
            return validDate(year, month, day);
        } else {
            return false;
        }
    }

    /**
     将15位身份证号码转换为18位

     @param idCard 15位身份编码
     @return 18位身份编码
     */
    public static String convert15CardTo18(String idCard) {
        String idCard18;
        if (idCard.length() != CHINA_ID_MIN_LENGTH) {
            return null;
        }
        if (isNum(idCard)) {
            // 获取出生年月日
            String birthday = idCard.substring(6, 12);
            int year = Integer.parseInt(birthday.substring(0, 2)) + 1900;
            String sYear = String.valueOf(year);
            idCard18 = idCard.substring(0, 6) + sYear + idCard.substring(8);
            // 转换字符数组
            char[] cArr = idCard18.toCharArray();
            int[] iCard = convertCharToInt(cArr);
            int iSum17 = getPowerSum(iCard);
            // 获取校验位
            String sVal = getCheckCode18(iSum17);
            if (!sVal.isEmpty()) {
                idCard18 += sVal;
            } else {
                return null;
            }
        } else {
            return null;
        }
        return idCard18;
    }

    /**
     将字符数组转换成数字数组

     @param ca 字符数组
     @return 数字数组
     */
    private static int[] convertCharToInt(char[] ca) {
        int len = ca.length;
        int[] iArr = new int[len];
        try {
            for (int i = 0; i < len; i++) {
                iArr[i] = Integer.parseInt(String.valueOf(ca[i]));
            }
        } catch (NumberFormatException e) {
            log.warn("将字符数组转换为数字数组时发生异常", e);
        }
        return iArr;
    }

    /**
     将身份证的每位和对应位的加权因子相乘之后，再得到和值

     @param iArr 身份证编码数组
     @return 加权和
     */
    private static int getPowerSum(int[] iArr) {
        int iSum = 0;
        if (POWER.length == iArr.length) {
            for (int i = 0; i < iArr.length; i++) {
                for (int j = 0; j < POWER.length; j++) {
                    if (i == j) {
                        iSum = iSum + iArr[i] * POWER[j];
                    }
                }
            }
        }
        return iSum;
    }

    /**
     将power和值与11取模获得余数进行校验码判断

     @param iSum 加权和
     @return 校验位
     */
    private static String getCheckCode18(int iSum) {
        return switch (iSum % 11) {
            case 10 -> "2";
            case 9 -> "3";
            case 8 -> "4";
            case 7 -> "5";
            case 6 -> "6";
            case 5 -> "7";
            case 4 -> "8";
            case 3 -> "9";
            case 2 -> "x";
            case 1 -> "0";
            case 0 -> "1";
            default -> "";
        };
    }

    /**
     验证小于当前日期 是否有效

     @param iYear  待验证日期(年)
     @param iMonth 待验证日期(月 1-12)
     @param iDate  待验证日期(日)
     @return 是否有效
     */
    private static boolean validDate(int iYear, int iMonth, int iDate) {
        int year = LocalDate.now()
                            .getYear();
        int datePerMonth;
        if (iYear < MIN || iYear >= year) {
            return false;
        }
        if (iMonth < 1 || iMonth > 12) {
            return false;
        }
        datePerMonth = switch (iMonth) {
            case 4, 6, 9, 11 -> 30;
            case 2 -> {
                boolean dm = (iYear % 4 == 0 && iYear % 100 != 0 || iYear % 400 == 0) && iYear > MIN;
                yield dm ? 29 : 28;
            }
            default -> 31;
        };
        return (iDate >= 1) && (iDate <= datePerMonth);
    }

    /**
     数字验证

     @param val 待验证字符串
     @return 是否为数字
     */
    private static boolean isNum(String val) {
        return val != null && !val.isEmpty() && val.matches("^[0-9]*$");
    }

    /**
     根据身份编号获取户籍省份

     @param idCard 身份编码
     @return 省级编码
     */
    public static String getProvinceByIdCard(String idCard) {
        int len = idCard.length();
        String sProvince;
        String sProvinceNum = "";
        if (len == CHINA_ID_MIN_LENGTH || len == CHINA_ID_MAX_LENGTH) {
            sProvinceNum = idCard.substring(0, 2);
        }
        sProvince = CITY_CODES.get(sProvinceNum);
        return sProvince;
    }

    /**
     获取年龄

     @param idCard 身份证号码
     @return 年龄
     */
    public static int getAge(String idCard) {
        LocalDate birthDate = getBirthday(idCard);
        if (birthDate == null) {
            return -1;
        }
        return Period.between(birthDate, LocalDate.now())
                     .getYears();
    }

    /**
     获取出生日期

     @param idCard 身份证号码
     @return 出生日期
     */
    public static LocalDate getBirthday(String idCard) {
        if (!validateCard(idCard)) {
            return null;
        }
        int lenOfCardId = idCard.length();
        if (lenOfCardId == CHINA_ID_MIN_LENGTH) {
            idCard = convert15CardTo18(idCard);
        }
        if (idCard != null) {
            try {
                String year = idCard.substring(6, 10);
                String month = idCard.substring(10, 12);
                String day = idCard.substring(12, 14);
                String birth = year + "-" + month + "-" + day;
                return LocalDate.parse(birth, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                log.warn("解析身份证出生日期时发生异常: idCard={}", idCard, e);
            }
        }
        return null;
    }

    /**
     根据证件类型枚举和证件号码识别出生日期

     @param idCardTypeEnum 证件类型枚举
     @param idCard         证件号码
     @return 出生日期，如果无法识别则返回null
     */
    public static LocalDate extractBirthDate(IdCardTypeEnum idCardTypeEnum, String idCard) {
        if (idCard == null || idCard.isEmpty() || idCardTypeEnum == null) {
            return null;
        }

        try {
            if (idCardTypeEnum.hasBirthDate()) {
                // 使用增强switch表达式处理不同证件类型的出生日期提取
                return switch (idCardTypeEnum) {
                    case ID_CARD ->
                        // 对于身份证，可以直接从号码中提取出生日期
                            extractBirthDateFromIdCard(idCard);
                    case RESIDENTS_PASSPORT_HK_MACAU ->
                        // 港澳居民来往内地通行证号码中包含出生日期信息
                            extractBirthDateFromHKMacauPass(idCard);
                    case RESIDENTS_PASSPORT_TAIWAN ->
                        // 台湾居民来往大陆通行证号码中包含出生日期信息
                            extractBirthDateFromTaiwanPass(idCard);
                    case HK_MACAU_ID_CARD, TAIWAN_ID_CARD ->
                        // 其他证件虽然有出生日期信息，但不能直接从号码中提取
                            null;
                    default -> null;
                };
            }
        } catch (Exception e) {
            log.warn("解析证件号码出生日期时发生异常: idCardTypeEnum={}, idCard={}", idCardTypeEnum, idCard, e);
        }

        return null;
    }

    /**
     根据证件类型枚举和证件号码识别性别

     @param idCardTypeEnum 证件类型枚举
     @param idCard         证件号码
     @return 性别枚举，如果无法识别则返回null
     */
    public static SexEnum extractGender(IdCardTypeEnum idCardTypeEnum, String idCard) {
        if (idCard == null || idCard.isEmpty() || idCardTypeEnum == null) {
            return null;
        }

        try {
            if (idCardTypeEnum.hasGender()) {
                // 使用增强switch表达式处理不同证件类型的性别提取
                return switch (idCardTypeEnum) {
                    case ID_CARD -> convertGender(extractGenderFromIdCard(idCard));
                    case RESIDENTS_PASSPORT_HK_MACAU -> convertGender(extractGenderFromHKMacauPass(idCard));
                    case RESIDENTS_PASSPORT_TAIWAN -> convertGender(extractGenderFromTaiwanPass(idCard));
                    case HK_MACAU_ID_CARD, TAIWAN_ID_CARD ->
                        // 其他证件虽然有性别信息，但不能直接从号码中提取
                            null;
                    default -> null;
                };
            }
        } catch (Exception e) {
            log.warn("解析证件号码性别时发生异常: idCardTypeEnum={}, idCard={}", idCardTypeEnum, idCard, e);
        }

        return null;
    }

    /**
     将数字性别代码转换为SexEnum枚举

     @param genderCode 性别代码（1:男, 0:女）
     @return SexEnum枚举，如果无法识别则返回null
     */
    private static SexEnum convertGender(Integer genderCode) {
        return genderCode != null ? (genderCode == 1 ? SexEnum.MAN : SexEnum.WOMAN) : null;
    }

    /**
     从身份证号码中提取出生日期

     @param idCard 身份证号码
     @return 出生日期
     */
    public static LocalDate extractBirthDateFromIdCard(String idCard) {
        if (!ID_CARD_18_PATTERN.matcher(idCard)
                               .matches() && !ID_CARD_15_PATTERN.matcher(idCard)
                                                                .matches()) {
            return null;
        }

        try {
            // 如果是15位身份证，先转换为18位
            if (idCard.length() == CHINA_ID_MIN_LENGTH) {
                idCard = convert15CardTo18(idCard);
            }
            // 18位身份证号码：第7位到第14位为出生日期（YYYYMMDD格式）
            String birthDateStr = null;
            if (idCard != null) {
                birthDateStr = idCard.substring(6, 14);
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            if (birthDateStr != null) {
                return LocalDate.parse(birthDateStr, formatter);
            }
        } catch (DateTimeParseException e) {
            log.warn("解析身份证出生日期时发生异常: idCard={}", idCard, e);
            return null;
        }
        return null;
    }

    /**
     从身份证号码中提取性别

     @param idCard 身份证号码
     @return 性别（1:男, 0:女）
     */
    public static Integer extractGenderFromIdCard(String idCard) {
        if (!ID_CARD_18_PATTERN.matcher(idCard)
                               .matches() && !ID_CARD_15_PATTERN.matcher(idCard)
                                                                .matches()) {
            return null;
        }

        try {
            // 如果是15位身份证，先转换为18位
            if (idCard.length() == CHINA_ID_MIN_LENGTH) {
                idCard = convert15CardTo18(idCard);
            }
            // 18位身份证号码：第17位数字用于表示性别，奇数为男，偶数为女
            int genderDigit = 0;
            if (idCard != null) {
                genderDigit = Integer.parseInt(String.valueOf(idCard.charAt(16)));
            }
            return genderDigit % 2 == 1 ? 1 : 0;
        } catch (Exception e) {
            log.warn("解析身份证性别时发生异常: idCard={}", idCard, e);
            return null;
        }
    }

    /**
     从港澳居民来往内地通行证号码中提取性别

     @param idCard 港澳居民来往内地通行证号码
     @return 性别（1:男, 0:女），如果无法识别则返回null
     */
    public static Integer extractGenderFromHKMacauPass(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return null;
        }

        try {
            // 港澳居民来往内地通行证格式：H/M + 8位数字 + (01) 或 8位数字 + H/M
            // 新版通行证号码中第8位数字表示性别，奇数为男，偶数为女
            int genderDigit = Integer.parseInt(String.valueOf(idCard.charAt(7)), 10);
            if (idCard.matches("^[HM]\\d{8}$")) {
                return genderDigit % 2 == 1 ? 1 : 0;
            }
            // 新版格式：H/M + 8位数字 + (01)
            else if (idCard.matches("^[HM]\\d{8}\\(\\d\\)$")) {
                return genderDigit % 2 == 1 ? 1 : 0;
            }
            // 旧版格式：8位数字 + H/M，第7位数字表示性别
            else if (idCard.matches("^\\d{8}[HM]$")) {
                int parseInt = Integer.parseInt(String.valueOf(idCard.charAt(6)), 10);
                return parseInt % 2 == 1 ? 1 : 0;
            }
        } catch (Exception e) {
            log.warn("解析港澳居民来往内地通行证性别时发生异常: idCard={}", idCard, e);
            return null;
        }

        return null;
    }

    /**
     从台湾居民来往大陆通行证号码中提取性别

     @param idCard 台湾居民来往大陆通行证号码
     @return 性别（1:男, 0:女），如果无法识别则返回null
     */
    public static Integer extractGenderFromTaiwanPass(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return null;
        }

        try {
            // 台湾居民来往大陆通行证格式：8位数字或10位数字
            // 8位数字格式中，第8位数字表示性别，奇数为男，偶数为女
            if (idCard.matches("^\\d{8}$")) {
                int genderDigit = Integer.parseInt(String.valueOf(idCard.charAt(7)), 10);
                return genderDigit % 2 == 1 ? 1 : 0;
            }
            // 10位数字格式中，第9位数字表示性别，奇数为男，偶数为女
            else if (idCard.matches("^\\d{10}$")) {
                int genderDigit = Integer.parseInt(String.valueOf(idCard.charAt(8)), 10);
                return genderDigit % 2 == 1 ? 1 : 0;
            }
        } catch (Exception e) {
            log.warn("解析台湾居民来往大陆通行证性别时发生异常: idCard={}", idCard, e);
            return null;
        }

        return null;
    }

    /**
     从港澳居民来往内地通行证号码中提取出生日期

     @param idCard 港澳居民来往内地通行证号码
     @return 出生日期，如果无法识别则返回null
     */
    public static LocalDate extractBirthDateFromHKMacauPass(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return null;
        }

        // 港澳居民来往内地通行证格式：H/M + 8位数字 + (01) 或 8位数字 + H/M
        // 新版通行证号码中第2-7位为出生日期（YYMMDD格式）
        try {
            String birthDateStr = extractBirthDateStrFromHKMacauPass(idCard);

            if (birthDateStr != null) {
                return parseBirthDate(birthDateStr);
            }
        } catch (DateTimeParseException e) {
            log.warn("解析港澳居民来往内地通行证出生日期时发生异常: idCard={}", idCard, e);
        }

        return null;
    }

    /**
     从港澳居民来往内地通行证号码中提取出生日期字符串

     @param idCard 港澳居民来往内地通行证号码
     @return 出生日期字符串，如果无法识别则返回null
     */
    private static String extractBirthDateStrFromHKMacauPass(String idCard) {
        // 处理新版格式：H/M + 8位数字
        if (idCard.matches("^[HM]\\d{8}$")) {
            return idCard.substring(1, 7);
        }
        // 处理新版格式：H/M + 8位数字 + (01)
        else if (idCard.matches("^[HM]\\d{8}\\(\\d\\)$")) {
            return idCard.substring(1, 7);
        }
        // 处理旧版格式：8位数字 + H/M
        else if (idCard.matches("^\\d{8}[HM]$")) {
            return idCard.substring(0, 6);
        }
        return null;
    }

    /**
     从台湾居民来往大陆通行证号码中提取出生日期

     @param idCard 台湾居民来往大陆通行证号码
     @return 出生日期，如果无法识别则返回null
     */
    public static LocalDate extractBirthDateFromTaiwanPass(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return null;
        }

        // 台湾居民来往大陆通行证格式：8位数字或10位数字
        // 8位数字格式中，前6位为出生日期（YYMMDD格式）
        // 10位数字格式中，第3-8位为出生日期（YYMMDD格式）
        try {
            String birthDateStr = extractBirthDateStrFromTaiwanPass(idCard);

            if (birthDateStr != null) {
                return parseBirthDate(birthDateStr);
            }
        } catch (DateTimeParseException e) {
            log.warn("解析台湾居民来往大陆通行证出生日期时发生异常: idCard={}", idCard, e);
        }

        return null;
    }

    /**
     从台湾居民来往大陆通行证号码中提取出生日期字符串

     @param idCard 台湾居民来往大陆通行证号码
     @return 出生日期字符串，如果无法识别则返回null
     */
    private static String extractBirthDateStrFromTaiwanPass(String idCard) {
        if (idCard.matches("^\\d{8}$")) {
            return idCard.substring(0, 6);
        } else if (idCard.matches("^\\d{10}$")) {
            return idCard.substring(2, 8);
        }
        return null;
    }

    /**
     解析出生日期字符串

     @param birthDateStr 出生日期字符串（YYMMDD格式）
     @return LocalDate对象
     */
    private static LocalDate parseBirthDate(String birthDateStr) {
        // 将YY格式转换为YYYY格式
        int year = Integer.parseInt(birthDateStr.substring(0, 2));
        year += year >= 50 ? 1900 : 2000;
        birthDateStr = year + birthDateStr.substring(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.parse(birthDateStr, formatter);
    }

    /**
     验证证件号码格式是否正确

     @param idCardTypeEnum 证件类型枚举
     @param idCard         证件号码
     @return 是否有效
     */
    public static boolean validateIdCard(IdCardTypeEnum idCardTypeEnum, String idCard) {
        if (idCard == null || idCard.isEmpty() || idCardTypeEnum == null) {
            return false;
        }

        return switch (idCardTypeEnum) {
            case ID_CARD ->
                // 18位身份证号码验证
                    ID_CARD_18_PATTERN.matcher(idCard)
                                      .matches() || ID_CARD_15_PATTERN.matcher(idCard)
                                                                      .matches();
            case RESIDENTS_PASSPORT_HK_MACAU ->
                // 港澳居民来往内地通行证
                    HK_MACAU_PASS_PATTERN.matcher(idCard)
                                         .matches();
            case RESIDENTS_PASSPORT_TAIWAN ->
                // 台湾居民来往大陆通行证
                    TAIWAN_PASS_PATTERN.matcher(idCard)
                                       .matches();
            case HK_MACAU_ID_CARD, TAIWAN_ID_CARD, OTHER ->
                // 对于其他证件类型，默认返回false，或者根据需要添加特定的验证逻辑
                // 目前只有身份证有明确的正则表达式验证
                    false;
        };
    }
}
