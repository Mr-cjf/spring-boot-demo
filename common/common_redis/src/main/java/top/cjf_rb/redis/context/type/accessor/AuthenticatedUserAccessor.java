package top.cjf_rb.redis.context.type.accessor;

import org.springframework.lang.NonNull;
import top.cjf_rb.core.context.AuthenticatedUser;

import java.io.Serializable;
import java.time.Duration;
import java.util.Optional;

/**
 当前用户的缓存信息

 @author cjf
 @since 1.0 */
public class AuthenticatedUserAccessor extends RedisPrefixAccessor<AuthenticatedUser> {

    private final String phoneNoKey = "phoneNo";
    private final String nameKey = "name";
    private final String statusKey = "status";

    /**
     @param expires 有效期
     */
    public AuthenticatedUserAccessor(Duration expires) {
        this("app-authenticatedUser:", expires);
    }

    /**
     @param prefix  key前缀
     @param expires 有效期
     */
    public AuthenticatedUserAccessor(String prefix, Duration expires) {
        super(prefix, expires);
    }

    /**
     设置用户名

     @param newName 新名字
     */
    public void resetName(@NonNull Serializable identifier, @NonNull String newName) {
        this.setValue(identifier, newName, nameKey);
    }

    /**
     设置手机号

     @param newPhoneNo 新手机号
     */
    public void resetPhoneNo(@NonNull Serializable identifier, @NonNull String newPhoneNo) {
        this.setValue(identifier, newPhoneNo, phoneNoKey);
    }

    /**
     设置用户状态

     @param newStatus 新用户状态
     */
    public void resetStatus(@NonNull Serializable identifier, @NonNull Boolean newStatus) {
        this.setValue(identifier, newStatus, statusKey);
    }

    /**
     重新设置值
     */
    private void setValue(Serializable identifier, Object value, String fieldKey) {
        String key = keyPrefix + identifier;

        Optional<AuthenticatedUser> optional = this.get(identifier);
        if (optional.isEmpty()) {
            return;
        }

        AuthenticatedUser authenticatedUser = optional.get();
        // 重设值
        if (phoneNoKey.equals(fieldKey)) {
            authenticatedUser.setPhoneNo((String) value);
        } else if (nameKey.equals(fieldKey)) {
            authenticatedUser.setName((String) value);
        } else if (statusKey.equals(fieldKey)) {
            authenticatedUser.setEnabled((Boolean) value);
        }

        this.set(key, authenticatedUser);
    }

}
