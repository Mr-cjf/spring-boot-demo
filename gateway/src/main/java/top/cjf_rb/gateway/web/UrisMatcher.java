package top.cjf_rb.gateway.web;

import lombok.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Collections;
import java.util.Set;

/**
 * @Author Zoe
 * @create 2024/3/18
 */
public class UrisMatcher {

    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final Set<String> paths;

    public UrisMatcher(@NonNull Set<String> paths) {
        this.paths = paths;
    }

    public static UrisMatcher any() {
        return new UrisMatcher(Set.of("/**"));
    }

    public static UrisMatcher none() {
        return new UrisMatcher(Collections.emptySet());
    }

    /**
     * 判断uri是否在集合中
     *
     * @param path 当前请求uri
     * @return 匹配上则true, 反之, false
     */
    public boolean match(@NonNull String path) {
        for (String pattern : paths) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }

        return false;
    }
}
