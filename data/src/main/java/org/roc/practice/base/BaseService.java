package org.roc.practice.base;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 预留拓展
 * 不做Mp方法收口
 * 此处拓展的方法必须对所有业务Service普遍适用
 * Service层非必须， 简单CRUD场景可由Controller直接使用IService实现
 */
public interface BaseService<T> extends IService<T> {
}
