package org.roc.practice.page;

import lombok.Data;

@Data
public class ScrollQuery {
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 200;

    /**
     * 游标ID，第一次请求传null
     * 后续请求从传上一次响应的nextCursor
     */
    private Long cursor;

    private Integer size = DEFAULT_SIZE;

    public Integer getSafeSize() {
        if(size == null||size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
