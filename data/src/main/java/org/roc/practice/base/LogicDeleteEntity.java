package org.roc.practice.base;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogicDeleteEntity extends BaseEntity {
    /**
     * 逻辑删除标志
     * 0 = 未删除， 1 = 已删除
     */
    @TableLogic
    private Integer deleted;
}