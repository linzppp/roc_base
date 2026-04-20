package org.roc.practice.page;

import lombok.Data;

import java.util.Set;

@Data
public class PageQuery {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 1000;

    private Integer current = DEFAULT_PAGE;
    private Integer size = DEFAULT_PAGE_SIZE;

    public Integer getSafeSize(){
        if(size == null|| size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    public Integer getSafeCurrent(){
        if(current == null || current <= 0){
            return DEFAULT_PAGE;
        }

        return current;
    }

}
