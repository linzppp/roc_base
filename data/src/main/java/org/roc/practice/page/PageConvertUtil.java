package org.roc.practice.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.roc.practice.result.PageVo;

public class PageConvertUtil {
    private PageConvertUtil() {}

    private static <T> PageVo<T> toPageVo(IPage<T> page){
        return PageVo.of(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );
    }

    private static<T> Page<T> toMpPage(PageQuery query){
        return toMpPage(query, null, false);
    }

    private static <T> Page<T> toMpPage(PageQuery query, String validatedOrderBy, boolean asc){
        Page<T> page = new Page<>(query.getSafeCurrent(), query.getSafeSize());

        if(validatedOrderBy != null){
            if(asc){
                page.addOrder(OrderItem.asc(validatedOrderBy));
            }else{
                page.addOrder(OrderItem.desc(validatedOrderBy));
            }
        }

        return page;
    }
}
