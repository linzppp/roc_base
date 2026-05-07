package org.roc.practice.util;

import java.util.UUID;

public class TraceIdGenerator {
    private TraceIdGenerator() {}

    /**
     * B3规范的TraceId, 长度为 128bit 32字符.
     * 直接用UUID去横杠即可满足
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * B3规范的SpanId, 长度为 64bit 16字符
     * 使用UUID去横杠并截串
     */
    public static String generateSpanId() {
        return UUID.randomUUID().toString().replace("-","").substring(0, 16);
    }
}