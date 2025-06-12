package top.cjf_rb.core.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import top.cjf_rb.core.constant.AppSystemConst;
import top.cjf_rb.core.constant.ClientAgentEnum;

import java.io.Serial;
import java.util.Collections;
import java.util.Set;

/**
 * 认证的用户信息
 *
 * @author lty
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class AuthenticatedUser implements UserDetails, CredentialsContainer {
    @Serial
    private static final long serialVersionUID = AppSystemConst.SERIAL_VERSION_UID;
    /**
     * 所属机构ID
     */
    private Integer agencyId;

    /**
     * 身份类型
     */
    private ClientAgentEnum clientAgent;

    /**
     * 是否已验证账号
     */
    private boolean verifyAccount = true;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名字
     */
    private String name;
    /**
     * 密码
     */
    private String password;

    /**
     * 绑定的手机号
     */
    private String phoneNo;

    /**
     * 用户的权限
     */
    private Set<? extends GrantedAuthority> authorities = Collections.emptySet();

    /**
     * 是否账号已过期
     */
    private boolean accountNonExpired = true;
    /**
     * 是否账号被锁定
     */
    private boolean accountNonLocked = true;
    /**
     * 是否凭证已过期
     */
    private boolean credentialsNonExpired = true;
    /**
     * 是否已启用
     */
    private boolean enabled = true;
    /**
     * 是否虚拟用户
     */
    private boolean virtual = false;
    /**
     * 用户其他信息
     */
    private Object details;

    public static AuthenticatedUser of(Long userId, String name, String phoneNo) {
        return new AuthenticatedUser().setUserId(userId).setName(name).setPhoneNo(phoneNo);
    }

    public static AuthenticatedUser of(Long userId, String name, String phoneNo,
                                       Set<? extends GrantedAuthority> authorities) {
        return new AuthenticatedUser().setUserId(userId).setName(name).setPhoneNo(phoneNo).setAuthorities(authorities);
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return userId.toString();
    }

    @Override
    public void eraseCredentials() {
        password = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AuthenticatedUser user) {
            return this.userId.equals(user.userId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.userId.hashCode();
    }

}
