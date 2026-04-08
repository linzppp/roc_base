package org.roc.practice.core.result;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于滑动查询的结果返回; 只适用于结果包含Id的情况; ID是Long类型 且 在结果中是有序的
 * list 结果数据
 * size 滑动查询的分页大小
 * nextCursor 滑动查询的下一起点(将被包含); 只适用于id按时间戳递增的场景
 * hasNext 是否有下一滑页
 *
 * 使用该类必须实现ScrollCursorTarget接口
 */
@Data
public class ScrollVO<T> implements Serializable {
    private List<T> list;
    private Integer size;
    private Long nextCursor;
    private Boolean hasNext;

    public static <T extends CursorTarget> ScrollVO<T> of(List<T> list, int size) {
        ScrollVO<T> vo = new ScrollVO<>();
        List<T> listCopy = new ArrayList<>(list);
        Boolean hasNext = list.size() > size;
        if (hasNext) {
            T item = listCopy.remove(list.size() - 1);
            Long id = item.getCursorPosition();
            vo.setNextCursor(id);
        } else {
            vo.setNextCursor(null);
        }
        vo.setList(listCopy);
        vo.setSize(size);
        vo.setHasNext(hasNext);
        return vo;
    }

}
