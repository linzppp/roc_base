package org.roc.practice.core.result;

//import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 *
 */
@Data
public class PageVO<T> implements Serializable {
    private List<T> list;
    // 总条目数
    private Long total;
    // 当前页码
    private Integer pageNum;
    // 页大小
    private Integer pageSize;
    // 总页数
    private Integer pages;

    public static<T> PageVO<T> of(List<T> list, long total, Integer pageNum, Integer pageSize){
        PageVO<T> vo = new PageVO<>();
        vo.setList(list);
        vo.setTotal(total);
        vo.setPageNum(pageNum);
        vo.setPageSize(pageSize);
        vo.setPages((int)Math.ceil((double)total/pageSize));
        return vo;
    }

//    public static<T> PageVO<T> from(IPage<T> page){
//        PageVO<T> vo = new PageVO<>();
//        vo.setList(page.getRecords());
//        vo.setTotal(page.getTotal());
//        vo.setPageSize((int)page.getSize());
//        vo.setPageNum((int)page.getCurrent());
//        vo.setPages((int)page.getPages());
//        return vo;
//    }
}
