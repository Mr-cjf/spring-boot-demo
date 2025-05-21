package top.cjf_rb.gateway.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 唯一标识符生成
 *
 * @author lty
 * @since 1.0
 */
public class Identifiers {

    /**
     * 更安全的UUID生成, 已移除默认的'-'
     *
     * @return 32位唯一标识字符串
     */
    public static String uuid() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new UUID(random.nextLong(), random.nextLong()).toString().replace("-", "");
    }

}
