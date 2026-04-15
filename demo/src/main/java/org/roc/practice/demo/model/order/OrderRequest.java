package org.roc.practice.demo.model.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.roc.practice.demo.model.user.UserRequest;
import org.roc.practice.validate.Create;
import org.roc.practice.validate.Update;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class OrderRequest {
    @NotNull
    @Valid
    private UserRequest custom;

    @Null(groups = {Create.class}, message = "订单ID在创建时不能赋值")
    @NotNull(groups = {Update.class}, message = "订单ID不能为空")
    private Long orderId;

    @Positive(message = "支付金额必须是正数")
    private Long price;

    @FutureOrPresent(message = "你只能订阅今天以后的")
    private LocalDate ts;
}
