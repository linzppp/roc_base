package org.roc.practice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Filter 级日志
 * 面向所有Request请求进行日志记录
 * 进行超时请求采用WARN级别, 其他非超时请求采用INFO级别记录
 * 通过X-Real-IP, X-Forwarded-For, RemoteAddr 进行请求IP记录
 * 未对Query进行脱敏处理.
 * 通过 excludedPrefixes 跳过监控/文档等基础设施端点，避免日志噪声.
 */
@Slf4j
public class AccessLogFilter extends OncePerRequestFilter {
    private static final long SLOW_REQUEST_THRESHOLD_MS = 1000;

    private final Set<String> excludedPrefixes;

    public AccessLogFilter(Set<String> excludedPrefixes) {
        this.excludedPrefixes = excludedPrefixes;
    }

    /**
     * 匹配任意排除前缀时跳过，doFilterInternal 不会被调用。
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return excludedPrefixes.stream().anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {
        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            String msg = "ACCESS | {} {} status={} elapsed={}ms ip={}";
            Object[] args = {
                    request.getMethod(),
                    buildUri(request),
                    response.getStatus(),
                    elapsed,
                    getClientIp(request)
            };
            if (elapsed >= SLOW_REQUEST_THRESHOLD_MS) {
                log.warn(msg, args);
            } else {
                log.info(msg, args);
            }
        }
    }

    private String buildUri(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        if (StringUtils.hasText(query)) {
            return String.format("%s?%s", uri, query);
        }
        return uri;
    }

    private String getClientIp(HttpServletRequest request) {
        String xri = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xri)) {
            return xri;
        }
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
