package top.cjf_rb.gateway.web.handler;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ClaimJwtException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import top.cjf_rb.gateway.constants.AppHeaderConst;
import top.cjf_rb.gateway.constants.GatewayErrorCodeEnum;
import top.cjf_rb.gateway.exception.CredentialsNotFoundException;
import top.cjf_rb.gateway.exception.ErrorCode;
import top.cjf_rb.gateway.exception.GatewayException;
import top.cjf_rb.gateway.pojo.vo.ErrorVo;

import java.nio.charset.StandardCharsets;

/**
 * <pre>
 *     <h2>网关层异常全局捕获处理</h2>
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @NonNull
    @Override
    public Mono<Void> handle(@NonNull ServerWebExchange exchange, @NonNull Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // jwt 异常
        if (ex instanceof JwtException je) {
            return this.onFailure(response, je);
        }

        // 网关内部异常
        if (ex instanceof GatewayException ge) {
            return this.onFailure(response, ge);
        }

        // 未知异常
        return this.onFailure(response, ex);
    }

    /**
     * jwt异常
     */
    protected Mono<Void> onFailure(ServerHttpResponse response, JwtException ex) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        if (ex instanceof ClaimJwtException) {
            if (ex instanceof ExpiredJwtException) {
                byte[] bytes = this.errorVoToBytes(GatewayErrorCodeEnum.LOGIN_EXPIRED);
                return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
            }
        }

        byte[] bytes = this.errorVoToBytes(GatewayErrorCodeEnum.LOGIN_INCORRECT);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    /**
     * 网关自身异常
     */
    protected Mono<Void> onFailure(ServerHttpResponse response, GatewayException ex) {
        String traceId = response.getHeaders().getFirst(AppHeaderConst.TRACE_ID);

        if (ex instanceof CredentialsNotFoundException) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            byte[] bytes = this.errorVoToBytes(GatewayErrorCodeEnum.NOT_LOGGED_IN);
            log.warn("[{}] 找不到凭证:{}", traceId, ex.getMessage());
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        }

        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        byte[] bytes = this.errorVoToBytes(GatewayErrorCodeEnum.GATEWAY_ERROR);
        log.error("[{}] 网关内部错误", traceId, ex);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    /**
     * 未知异常
     */
    protected Mono<Void> onFailure(ServerHttpResponse response, Throwable ex) {
        String traceId = response.getHeaders().getFirst(AppHeaderConst.TRACE_ID);
        response.setStatusCode(HttpStatus.BAD_GATEWAY);

        byte[] bytes = this.errorVoToBytes(GatewayErrorCodeEnum.UNKNOWN_ERROR);

        log.error("[{}] 未知异常", traceId, ex);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    /**
     * {@link ErrorVo}转 Byte
     */
    private byte[] errorVoToBytes(ErrorCode errorCode) {
        try {
            ErrorVo errorVo = ErrorVo.of(errorCode);
            return objectMapper.writeValueAsString(errorVo).getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            throw new GatewayException("Jackson 序列化异常", e);
        }
    }

}
