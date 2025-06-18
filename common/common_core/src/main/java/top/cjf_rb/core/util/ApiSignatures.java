package top.cjf_rb.core.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.lang.NonNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 项目常规签名
 */
public final class ApiSignatures {
    public static final String DELIMITER = "|";

    /**
     * 签名流程: 以'|'分隔符拼接所有非null字符串, 进行base64编码, 再进行MD5加密得到签名
     *
     * @param args 签名参数
     * @return MD5签名
     */
    public static String sign(@NonNull String secret, @NonNull List<String> args) {
        if (args.isEmpty()) {
            throw new IllegalArgumentException("the signature argument must be empty!");
        }

        // 过滤null, 且排序
        String sequence = args.stream()
                              .filter(Objects::nonNull)
                              .collect(Collectors.joining(DELIMITER));

        // 追加密钥
        sequence = String.join(DELIMITER, sequence, secret); return encodeMd5Hex(sequence);
    }

    /**
     * 验证签名签名
     *
     * @param args      签名参数
     * @param signature 签名
     * @return 验签通过则true, 反之, false
     */
    public static boolean verify(@NonNull String secret, @NonNull String signature, @NonNull List<String> args) {
        String sign = sign(secret, args); return sign.equals(signature);
    }

    /**
     * base64编码, 再Md5加密得到签名
     *
     * @param text 需要签名加密的文本数据
     * @return MD5签名
     */
    private static String encodeMd5Hex(@NonNull String text) {
        byte[] encode = Base64.getEncoder()
                              .encode(text.getBytes(StandardCharsets.UTF_8)); return DigestUtils.md5Hex(encode);
    }


}
