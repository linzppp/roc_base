package org.roc.practice.constants;

public final class TraceConstants {
    // MDC使用
    public static final String TRACE_ID = "traceId";
    public static final String SPAN_ID = "spanId";
    public static final String PARENT_SPAN_ID = "parentSpanId";
    public static final String USER_ID = "userId";
    public static final String APP_NAME = "appName";

    // Header使用
    public static final String HEADER_TRACE_ID = "X-B3-TraceId";
    public static final String HEADER_SPAN_ID = "X-B3-SpanId";
    public static final String HEADER_PARENT_SPAN_ID = "X-B3-Parent-SpanId";
    public static final String HEADER_SAMPLED = "X-B3-Sampled";

}
