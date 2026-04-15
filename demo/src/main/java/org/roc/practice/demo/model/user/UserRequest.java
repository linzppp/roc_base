package org.roc.practice.demo.model.user;

import jakarta.validation.constraints.*;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.roc.practice.validate.Create;
import org.roc.practice.validate.Update;

@Data
@AllArgsConstructor
public class UserRequest {
    @Null(groups = {Create.class}, message = "用户ID在创建时不能赋值")
    @NotNull(groups = {Update.class, Default.class}, message = "用户ID不能为空")
    private Long userId;
    @NotBlank(message = "未填写用户姓名")
    private String userName;
    @Min(value = 0, message = "年龄应大于0岁")
    @Max(value = 199, message = "年龄不能超过199岁")
    private Integer age;
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入手机号, 应符合中国大陆手机号规格, 11位")
    @NotNull(message = "手机号不能为空")
    private String phone;
    @NotNull
    @Length(min = 10, max = 10)
    private String password;
}
