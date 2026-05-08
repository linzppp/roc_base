package org.roc.practice.filter;

import org.springframework.util.StringUtils;;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.roc.practice.constants.TraceConstants;
import org.roc.practice.util.TraceIdGenerator;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class TraceIdFilter extends OncePerRequestFilter {
    private final String appName;

    public TraceIdFilter(String appName) {
        this.appName = appName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            initMdc(request);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private void initMdc(HttpServletRequest request) {
        String traceId = request.getHeader(TraceConstants.HEADER_TRACE_ID);
        if (!StringUtils.hasText(traceId)) {
            traceId = TraceIdGenerator.generateTraceId();
        }

        // 上游 SpanId 作为本次调用的 parentSpanId，本服务生成新的 SpanId
        String parentSpanId = request.getHeader(TraceConstants.HEADER_SPAN_ID);

        String spanId = TraceIdGenerator.generateSpanId();

        MDC.put(TraceConstants.TRACE_ID, traceId);
        MDC.put(TraceConstants.SPAN_ID, spanId);
        if (StringUtils.hasText(parentSpanId)) {
            MDC.put(TraceConstants.PARENT_SPAN_ID, parentSpanId);
        }
        MDC.put(TraceConstants.APP_NAME, appName);
    }
}
