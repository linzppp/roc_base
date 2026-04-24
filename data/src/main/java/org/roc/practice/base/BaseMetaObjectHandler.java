package org.roc.practice.base;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class BaseMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject){
        LocalDateTime now = LocalDateTime.now();

        // strict操作
        // 只在字段为null时填充， 允许业务预填不覆盖
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);

        Long currentUserId =  getCurrentUserId();
        this.strictInsertFill(metaObject, "createBy", Long.class, currentUserId);
        this.strictInsertFill(metaObject, "updateBy", Long.class, currentUserId);
    }

    @Override
    public void updateFill(MetaObject metaObject){
        LocalDateTime now = LocalDateTime.now();
        Long userId = getCurrentUserId();

        // set操作
        // 强制赋值，不允许业务预填
        this.setFieldValByName("updateTime", now, metaObject);
        this.setFieldValByName("updateBy", userId, metaObject);
    }

    private Long getCurrentUserId(){
        // TODO
        return null;
    }
}
