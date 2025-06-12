package top.cjf_rb.core.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import top.cjf_rb.core.pojo.prop.AppSecurityProperties;
import top.cjf_rb.core.support.AuthUserAccessor;
import top.cjf_rb.core.web.security.AppAuthenticationEntryPoint;
import top.cjf_rb.core.web.security.HttpStatusAccessDeniedHandler;
import top.cjf_rb.core.web.security.TransferAuthenticationConfig;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Import({AppSecurityProperties.class})
public class AppSecurityConfig {
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private AppSecurityProperties appSecurityProperties;
    @Resource
    private AuthUserAccessor authUserAccessor;

    /**
     * 提供默认的{@link HttpSecurity}配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 禁用默认
        http.securityContext(AbstractHttpConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);
        http.anonymous(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.cors(Customizer.withDefaults());

        // 注销
        http.logout(AbstractHttpConfigurer::disable);

        // 不需要 session
        http.sessionManagement(AbstractHttpConfigurer::disable);

        // 无需鉴权
        http.authorizeHttpRequests((authorizeRequests) -> authorizeRequests.requestMatchers(HttpMethod.OPTIONS)
                .permitAll()
                .anyRequest().permitAll());

        // 认证/鉴权异常
        http.exceptionHandling((exceptionHandling) -> exceptionHandling
                .authenticationEntryPoint(authenticationEntryPoint()).accessDeniedHandler(accessDeniedHandler()));

        // http 响应头
        http.headers((headers) -> headers.defaultsDisabled().cacheControl(Customizer.withDefaults())
                .frameOptions(Customizer.withDefaults()));

        // 自定义的认证流程：验证登录
        TransferAuthenticationConfig authenticationConfig =
                TransferAuthenticationConfig.getInstance(failureHandler(), authUserAccessor);
        http.with(authenticationConfig, Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        String[] permitUris = appSecurityProperties.getPermitUris();
        String[] permitStaticUris = appSecurityProperties.getPermitStaticUris();
        String[] permitActuatorUris = appSecurityProperties.getPermitActuatorUris();
        // 设置全局静态资源忽略
        return (web) -> web.ignoring().requestMatchers(permitUris).requestMatchers(permitStaticUris)
                .requestMatchers(permitActuatorUris);
    }

    /**
     * 认证失败处理
     */
    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return new AuthenticationEntryPointFailureHandler(authenticationEntryPoint());
    }

    /**
     * 身份认证错误处理
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new AppAuthenticationEntryPoint(objectMapper);
    }

    /**
     * 默认的无权限访问被拒绝的处理程序
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new HttpStatusAccessDeniedHandler(HttpStatus.FORBIDDEN);
    }

    /**
     * 默认的密码器
     *
     * @see org.springframework.security.crypto.factory.PasswordEncoderFactories
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        String encodingId = "bcrypt";
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(encodingId, new BCryptPasswordEncoder());
        encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("scrypt", SCryptPasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("argon2", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        return new DelegatingPasswordEncoder(encodingId, encoders);
    }

}
