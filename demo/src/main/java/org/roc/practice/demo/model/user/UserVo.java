package org.roc.practice.demo.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.roc.practice.page.ICursor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo implements ICursor {
    private Long userId;
    private String userName;

    @Override
    public Long getCursorId() {
        return userId;
    }
}
