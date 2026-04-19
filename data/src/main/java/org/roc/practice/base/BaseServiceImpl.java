package org.roc.practice.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.roc.practice.mapper.BaseMapper;

/**
 * 框架基础Service实现， 所有业务ServiceImpl需继承此类
 *
 * @param <M> 对应Mapper
 * @param <T> 对应Entity
 */
public class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity>
        extends ServiceImpl<M, T>
        implements BaseService<T> {

}
