package top.cjf_rb.core.web.security;


import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import top.cjf_rb.core.support.AuthUserAccessor;


@RequiredArgsConstructor
public class TransferAuthenticationConfig extends AbstractHttpConfigurer<TransferAuthenticationConfig, HttpSecurity> {

    private final AuthenticationFailureHandler failureHandler;
    private final AuthUserAccessor authUserAccessor;

    public static TransferAuthenticationConfig getInstance(AuthenticationFailureHandler failureHandler,
                                                           AuthUserAccessor authUserAccessor) {
        return new TransferAuthenticationConfig(failureHandler, authUserAccessor);
    }

    @Override
    public void configure(HttpSecurity http) {
        TransferAuthenticationFilter authFilter = new TransferAuthenticationFilter(failureHandler, authUserAccessor);
        http.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
