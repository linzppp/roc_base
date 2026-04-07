package org.roc.practice.core.result;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用于滑动查询的结果返回; 只适用于结果包含Id的情况; ID是Long类型 且 在结果中是有序的
 * list 结果数据
 * size 滑动查询的分页大小
 * nextCursor 滑动查询的下一起点(将被包含); 只适用于id按时间戳递增的场景
 * hasNext 是否有下一滑页
 */
@Data
public class ScrollVO<T> implements Serializable {
    private List<T> list;
    private Integer size;
    private Long nextCursor;
    private Boolean hasNext;

    public static <T> ScrollVO<T> of(List<T> list, int size) {
        ScrollVO<T> vo = new ScrollVO<>();
        Boolean hasNext = list.size() > size;
        if (hasNext) {
            T item = list.remove(list.size() - 1);
            Long id = getId(item);
            vo.setNextCursor(id);
        } else {
            vo.setNextCursor(null);
        }
        vo.setList(list);
        vo.setSize(size);
        vo.setHasNext(hasNext);
        return vo;
    }

    private static Long getId(Object obj) {
        try {
            return (Long) obj.getClass().getMethod("getId").invoke(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
