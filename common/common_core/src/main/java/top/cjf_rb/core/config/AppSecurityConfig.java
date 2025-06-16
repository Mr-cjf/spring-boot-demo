package top.cjf_rb.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AppSecurityConfig {
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private AppSecurityProperties appSecurityProperties;

    @Bean
    public TransferAuthenticationConfig transferAuthenticationConfig(AuthenticationFailureHandler failureHandler,
                                                                     AuthUserAccessor authUserAccessor) {
        return new TransferAuthenticationConfig(failureHandler, authUserAccessor);
    }


    /**
     * 提供默认的{@link HttpSecurity}配置
     */
    @Bean
    @Order(1) // 设置为最高优先级
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   TransferAuthenticationConfig transferAuthenticationConfig) throws Exception {
        // 禁用默认安全机制
        http.securityMatcher("/**") // 明确匹配所有路径
            .securityContext(AbstractHttpConfigurer::disable)
            .csrf(AbstractHttpConfigurer::disable)
            .anonymous(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(AbstractHttpConfigurer::disable);
        // 添加 URL 匹配规则
        http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.requestMatchers(HttpMethod.OPTIONS)
                                                                                 .permitAll()
                                                                                 .anyRequest()
                                                                                 .authenticated());

        // 异常处理
        http.exceptionHandling(
                (exceptionHandling) -> exceptionHandling.authenticationEntryPoint(authenticationEntryPoint())
                                                        .accessDeniedHandler(accessDeniedHandler()));

        // 响应头设置
        http.headers((headers) -> headers.defaultsDisabled()
                                         .cacheControl(Customizer.withDefaults())
                                         .frameOptions(Customizer.withDefaults()));
        http.with(transferAuthenticationConfig, Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        String[] permitUris = appSecurityProperties.getPermitUris();
        String[] permitStaticUris = appSecurityProperties.getPermitStaticUris();
        String[] permitActuatorUris = appSecurityProperties.getPermitActuatorUris();
        // 设置全局静态资源忽略
        return (web) -> web.ignoring()
                           .requestMatchers(permitUris)
                           .requestMatchers(permitStaticUris)
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