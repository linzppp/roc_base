package org.roc.practice.base;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogicDeleteEntity extends BaseEntity{
    @TableLogic
    private Integer deleted;
}
