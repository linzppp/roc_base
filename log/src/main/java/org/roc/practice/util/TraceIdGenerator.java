package org.roc.practice.util;

import java.util.concurrent.ThreadLocalRandom;

public class TraceIdGenerator {
    private TraceIdGenerator() {}

    /**
     * B3规范的TraceId，128bit，32位小写十六进制字符串。
     * 使用 ThreadLocalRandom 替代 UUID（SecureRandom），避免高并发下熵池竞争。
     */
    public static String generateTraceId() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return String.format("%016x%016x", r.nextLong(), r.nextLong());
    }

    /**
     * B3规范的SpanId，64bit，16位小写十六进制字符串。
     */
    public static String generateSpanId() {
        return String.format("%016x", ThreadLocalRandom.current().nextLong());
    }
}