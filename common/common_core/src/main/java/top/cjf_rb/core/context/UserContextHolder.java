package top.cjf_rb.core.context;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import top.cjf_rb.core.util.Webs;

import javax.security.auth.login.AccountExpiredException;
import java.util.Optional;

/**
 @author cjf
 @since 1.0 */
public class UserContextHolder {

    public static final Long systemUserid = 1010101010101L;
    public static final Long anonymousUserid = 1234567891001L;
    public static final String virtualPhoneNo = "00000000000";

    private static final AuthenticatedUser ANONYMOUS_USER = new AuthenticatedUser().setUserid(anonymousUserid)
                                                                                   .setName("Anonymous")
                                                                                   .setPhoneNo(virtualPhoneNo)
                                                                                   .setVirtual(true);
    private static final AuthenticatedUser SYSTEM_USER = new AuthenticatedUser().setUserid(systemUserid)
                                                                                .setName("System")
                                                                                .setPhoneNo(virtualPhoneNo)
                                                                                .setVirtual(true);

    /**
     获取当前认证用户

     @return 在消息通讯的情况下, Authentication是为null
     */
    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext()
                                                        .getAuthentication());
    }

    /**
     获取当前用户的唯一标识

     @return 唯一标识, 在消息通讯或者未登录时, 将返回固定标识
     */
    @NonNull
    public static Long getPrincipal() {
        Optional<Authentication> optional = getAuthentication();
        if (optional.isEmpty()) {
            return ANONYMOUS_USER.getUserid();
        }

        Authentication authentication = optional.get();
        if (!authentication.isAuthenticated()) {
            return ANONYMOUS_USER.getUserid();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long userid) {
            return userid;
        }

        return Long.valueOf(principal.toString());
    }

    /**
     是否已登录
     */
    public static boolean isLoggedIn() {
        Optional<Authentication> optional = getAuthentication();
        // 无凭证为未登录
        if (optional.isEmpty()) {
            return false;
        }

        Authentication authentication = optional.get();
        // 匿名用户算是未登录
        if (authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }

        return authentication.isAuthenticated();
    }

    /**
     获取当前用户缓存的详细信息

     @return {@link AuthenticatedUser}
     */
    public static AuthenticatedUser getCurrentUser() {
        // 无身份凭证都为匿名用户
        Optional<Authentication> optional = getAuthentication();
        if (optional.isEmpty()) {
            Optional<HttpServletRequest> requestOptional = Webs.getRequest();
            if (requestOptional.isEmpty()) {
                return SYSTEM_USER;
            }

            return ANONYMOUS_USER;
        }

        // 有身份凭证但未认证状态或是匿名用户凭证都为匿名用户
        Authentication authentication = optional.get();
        if (!authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return ANONYMOUS_USER;
        }

        // 已认证用户
        return (AuthenticatedUser) authentication.getDetails();
    }

    /**
     校验用户
     */
    public static void verifyUser(UserDetails userDetails) throws AccountExpiredException {
        if (!userDetails.isAccountNonLocked()) {
            throw new LockedException("User account is locked");
        }

        if (!userDetails.isEnabled()) {
            throw new DisabledException("User is disabled");
        }

        if (!userDetails.isAccountNonExpired()) {
            throw new AccountExpiredException("User account has expired");
        }

        if (!userDetails.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("User credentials have expired");
        }
    }

}
