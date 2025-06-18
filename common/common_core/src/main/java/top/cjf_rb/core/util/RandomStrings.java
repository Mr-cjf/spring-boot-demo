package top.cjf_rb.core.util;

import org.apache.commons.text.RandomStringGenerator;

/**
 * 随机字符串生成
 */
public final class RandomStrings {

    /**
     * 随机字母,数字字符串生成器
     */
    private static final RandomStringGenerator ALPHANUMERIC_GENERATOR;
    /**
     * 随机数字字符串生成器
     */
    private static final RandomStringGenerator NUMERIC_GENERATOR;
    /**
     * 随机字母字符串生成器
     */
    private static final RandomStringGenerator ALPHABETIC_GENERATOR;
    /**
     * 随机数字,小写字母字符串生成器
     */
    private static final RandomStringGenerator ALPHABETIC_LC_GENERATOR;

    static {
        char[][] pairs = {{'a', 'z'}, {'A', 'Z'}, {'0', '9'}};
        ALPHANUMERIC_GENERATOR = new RandomStringGenerator.Builder().withinRange(pairs)
                                                                    .get();

        NUMERIC_GENERATOR = new RandomStringGenerator.Builder().withinRange('0', '9')
                                                               .get();

        char[][] alphanumericLcPairs = {{'a', 'z'}, {'0', '9'}};
        ALPHABETIC_LC_GENERATOR = new RandomStringGenerator.Builder().withinRange(alphanumericLcPairs)
                                                                     .get();

        char[][] alphabeticPairs = {{'a', 'z'}, {'A', 'Z'}};
        ALPHABETIC_GENERATOR = new RandomStringGenerator.Builder().withinRange(alphabeticPairs)
                                                                  .get();
    }

    /**
     * 随机生成指定位数的数字(0~9)字符串
     *
     * @param count 要创建的随机字符串的长度
     * @return 随机字符串
     */
    public static String randomNumeric(final int count) {
        return NUMERIC_GENERATOR.generate(count);
    }

    /**
     * 随机生成指定位数的数字(0~9)字符串
     *
     * @param minLengthInclusive 要生成的字符串的最小长度
     * @param maxLengthExclusive 要生成的字符串的最大长度
     * @return 随机字符串
     */
    public static String randomNumeric(final int minLengthInclusive, final int maxLengthExclusive) {
        return NUMERIC_GENERATOR.generate(minLengthInclusive, maxLengthExclusive);
    }

    /**
     * 随机生成指定位数的字母(a~zA~Z)字符串
     *
     * @param count 要创建的随机字符串的长度
     * @return 随机字符串
     */
    public static String randomAlphabetic(final int count) {
        return ALPHABETIC_GENERATOR.generate(count);
    }

    /**
     * 随机生成指定位数的字母(a~zA~Z)字符串
     *
     * @param minLengthInclusive 要生成的字符串的最小长度
     * @param maxLengthExclusive 要生成的字符串的最大长度
     * @return 随机字符串
     */
    public static String randomAlphabetic(final int minLengthInclusive, final int maxLengthExclusive) {
        return ALPHABETIC_GENERATOR.generate(minLengthInclusive, maxLengthExclusive);
    }

    /**
     * 随机生成指定位数的字母和数字(a~z0~9)字符串
     *
     * @param count 要创建的随机字符串的长度
     * @return 随机字符串
     */
    public static String randomAlphanumericLc(final int count) {
        return ALPHABETIC_LC_GENERATOR.generate(count);
    }

    /**
     * 随机生成指定位数的字母和数字(a~z0~9)字符串
     *
     * @param minLengthInclusive 要生成的字符串的最小长度
     * @param maxLengthExclusive 要生成的字符串的最大长度
     * @return 随机字符串
     */
    public static String randomAlphanumericLc(final int minLengthInclusive, final int maxLengthExclusive) {
        return ALPHABETIC_LC_GENERATOR.generate(minLengthInclusive, maxLengthExclusive);
    }

    /**
     * 随机生成指定位数的字母和数字(a~zA~Z0~9)字符串
     *
     * @param count 要创建的随机字符串的长度
     * @return 随机字符串
     */
    public static String randomAlphanumeric(final int count) {
        return ALPHANUMERIC_GENERATOR.generate(count);
    }

    /**
     * 随机生成指定位数的字母和数字(a~zA~Z0~9)字符串
     *
     * @param minLengthInclusive 要生成的字符串的最小长度
     * @param maxLengthExclusive 要生成的字符串的最大长度
     * @return 随机字符串
     */
    public static String randomAlphanumeric(final int minLengthInclusive, final int maxLengthExclusive) {
        return ALPHANUMERIC_GENERATOR.generate(minLengthInclusive, maxLengthExclusive);
    }

}
