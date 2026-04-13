package org.roc.practice.result;


import lombok.Data;
import org.roc.practice.page.ICursor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScrollVo<T> implements Serializable {
    private List<T> records;
    private Integer size;
    private Long nextCursor;
    private Boolean hasNext;

    public static <T extends ICursor> ScrollVo<T> of(List<T> list, Integer size) {
        ScrollVo<T> vo = new ScrollVo<>();
        List<T> listCopy = new ArrayList<>(list);
        Boolean hasNext = list.size() > size;
        if (hasNext) {
            T item = listCopy.remove(list.size() - 1);
            Long id = item.getCursorId();
            vo.setNextCursor(id);
        } else {
            vo.setNextCursor(null);
        }
        vo.setRecords(listCopy);
        vo.setSize(size);
        vo.setHasNext(hasNext);
        return vo;
    }
}
