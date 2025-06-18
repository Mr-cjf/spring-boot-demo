package top.cjf_rb.core.util;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 唯一标识符生成
 */
public final class Identifiers {

    public static final char[] NANOID_DEFAULT_ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    public static final char[] NANOID_LC_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

    /**
     * 更安全的UUID生成, 已移除默认的'-'
     *
     * @return 32位唯一标识字符串
     */
    public static String uuid() {
        ThreadLocalRandom random = ThreadLocalRandom.current(); return new UUID(random.nextLong(),
                                                                                random.nextLong()).toString()
                                                                                                  .replace("-", "");
    }

    /**
     * 一个小巧、安全、URL友好、唯一的字符串ID生成器 <a href="https://github.com/ai/nanoid/blob/main/README.zh-CN.md">Nano ID</a>
     * <p/>
     * 计算发生碰撞的概率: <a href="https://alex7kom.github.io/nano-nanoid-cc/">Nano ID Collision Calculator</a>
     *
     * @return 21位唯一标识字符串
     */
    public static String nanoId() {
        return NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, NANOID_DEFAULT_ALPHABET,
                                        NanoIdUtils.DEFAULT_SIZE);
    }

    /**
     * 指定长度的Nano Id, 字符串取值范围: [0~9a~zA~Z] <a href="https://github.com/ai/nanoid/blob/main/README.zh-CN.md">Nano ID</a>
     * <p/>
     * 计算发生碰撞的概率: <a href="https://alex7kom.github.io/nano-nanoid-cc/">Nano ID Collision Calculator</a>
     *
     * @param length 字符串长度, 但不能少于8
     * @return 指定长度的字符串
     */
    public static String nanoId(int length) {
        int minDigit = 8; if (length < minDigit) {
            length = minDigit;
        }

        return NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, NANOID_DEFAULT_ALPHABET, length);
    }

    /**
     * 指定长度的Nano Id, 字符串取值: [0~9a~z]
     *
     * @param length 字符串长度, 但不能少于8
     * @return 指定长度的字符串
     */
    public static String lcNanoId(int length) {
        int minDigit = 8; if (length < minDigit) {
            length = minDigit;
        }

        return NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, NANOID_LC_ALPHABET, length);
    }

}