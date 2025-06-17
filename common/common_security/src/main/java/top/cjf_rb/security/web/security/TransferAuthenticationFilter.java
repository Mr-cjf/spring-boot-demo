package top.cjf_rb.security.web.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.filter.OncePerRequestFilter;
import top.cjf_rb.core.constant.AppHeaderConst;
import top.cjf_rb.core.constant.ClientAgentEnum;
import top.cjf_rb.security.pojo.bo.AuthenticatedUserBo;
import top.cjf_rb.security.support.AuthUserAccessor;
import top.cjf_rb.core.util.Nones;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * 认证过滤器
 */
@RequiredArgsConstructor
public class TransferAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationFailureHandler failureHandler;
    private final AuthUserAccessor authUserAccessor;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        System.out.println("TransferAuthenticationFilter 正在处理请求: " + request.getRequestURI());

        // 是否需要认证
        if (!authenticationIsRequired()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 解析并认证
            this.doAuthentication(request);

            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();
            failureHandler.onAuthenticationFailure(request, response, e);
        } catch (Exception e) {
            AuthenticationException exception = new InternalAuthenticationServiceException("Authentication Error", e);
            failureHandler.onAuthenticationFailure(request, response, exception);
        }
    }

    /**
     * 解析认证信息
     */
    private void doAuthentication(HttpServletRequest request) {
        String userIdStr = request.getHeader(AppHeaderConst.CURRENT_USER);
        String clientAgent = request.getHeader(AppHeaderConst.CLIENT_AGENT);
        if (Nones.isBlank(userIdStr) || Nones.isBlank(clientAgent)) {
            String formatted = MessageFormat.format("请求头缺失，{0}={1}, {2}={3}", AppHeaderConst.CURRENT_USER,
                                                    userIdStr, AppHeaderConst.CLIENT_AGENT, clientAgent);
            throw new InternalAuthenticationServiceException(formatted);
        }

        // 校验 userId 必须为正整数
        if (!userIdStr.matches("\\d+")) {
            throw new IllegalArgumentException("非法的用户ID: " + userIdStr);
        }

        // 校验 clientAgent 是否为合法枚举值
        try {
            ClientAgentEnum agentEnum = ClientAgentEnum.valueOf(clientAgent.toUpperCase());

            // 显式转换为 Long 防止后续误用
            Long userId = Long.parseLong(userIdStr);

            AuthenticatedUserBo authUser = authUserAccessor.get(agentEnum, userId)
                                                           .orElseThrow(() -> new InternalAuthenticationServiceException(
                                                                 "用户不存在: " + userIdStr));

            // 构建已认证的 AuthenticationToken
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authUser.getUsername(), null, authUser.getAuthorities());
            authentication.setDetails(authUser);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
        } catch (IllegalArgumentException e) {
            throw new InternalAuthenticationServiceException("非法的客户端代理类型或用户输入: " + clientAgent, e);
        }
    }


    /**
     * 是否需要认证
     */
    private boolean authenticationIsRequired() {
        Authentication existingAuth = SecurityContextHolder.getContext()
                                                           .getAuthentication();
        if (existingAuth == null || !existingAuth.isAuthenticated()) {
            return true;
        }

        return (existingAuth instanceof AnonymousAuthenticationToken);
    }
}
