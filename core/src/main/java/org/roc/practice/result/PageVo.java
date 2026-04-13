package org.roc.practice.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageVo<T> implements Serializable {
    private List<T> records;
    // 总条目数
    private Long total;
    // 当前页码
    private Integer current;
    // 页大小
    private Integer size;

    public static<T> PageVo<T> of(List<T> list, long total, Integer current, Integer size){
        PageVo<T> vo = new PageVo<>();
        vo.setRecords(list);
        vo.setTotal(total);
        vo.setSize(size);
        vo.setCurrent(current);
        return vo;
    }
}