package org.roc.practice.demo;

import jakarta.validation.Valid;
import org.roc.practice.constant.CommonResultCode;
import org.roc.practice.demo.model.order.OrderRequest;
import org.roc.practice.demo.model.user.UserRequest;
import org.roc.practice.demo.model.user.UserVo;
import org.roc.practice.exception.BusinessException;
import org.roc.practice.result.PageVo;
import org.roc.practice.result.Result;
import org.roc.practice.result.ScrollVo;
import org.roc.practice.validate.Create;
import org.roc.practice.validate.Update;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.LongStream;

@RestController
@RequestMapping("/demo")
public class DemoController {

    /** 核验点1: 统一返回 Result — GET /demo/hello */
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Hello, roc-base!");
    }

    /** 核验点2: Create 分组校验，userId 必须为 null — POST /demo/user/create */
    @PostMapping("/user/create")
    public Result<Void> createUser(@RequestBody @Validated(Create.class) UserRequest request) {
        return Result.success();
    }

    /** 核验点3: Update 分组校验，userId 不能为 null — PUT /demo/user/update */
    @PutMapping("/user/update")
    public Result<Void> updateUser(@RequestBody @Validated(Update.class) UserRequest request) {
        return Result.success();
    }

    /** 核验点4: 嵌套校验，@Valid 级联校验内部 UserRequest — POST /demo/order/create */
    @PostMapping("/order/create")
    public Result<Void> createOrder(@RequestBody @Validated(Create.class) @Valid OrderRequest request) {
        return Result.success();
    }

    /** 核验点5: 偏移分页 PageVo — GET /demo/page?current=1&size=10 */
    @GetMapping("/page")
    public Result<PageVo<UserVo>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        List<UserVo> records = LongStream
                .rangeClosed((long) (current - 1) * size + 1, (long) current * size)
                .mapToObj(i -> new UserVo(i, "用户" + i))
                .toList();
        return Result.success(PageVo.of(records, 100, current, size));
    }

    /** 核验点6: 游标分页 ScrollVo — GET /demo/scroll?cursor=0&size=5 */
    @GetMapping("/scroll")
    public Result<ScrollVo<UserVo>> scroll(
            @RequestParam(defaultValue = "0") Long cursor,
            @RequestParam(defaultValue = "5") Integer size) {
        List<UserVo> records = LongStream
                .rangeClosed(cursor + 1, cursor + size + 1)
                .mapToObj(i -> new UserVo(i, "用户" + i))
                .toList();
        return Result.success(ScrollVo.of(records, size));
    }

    /** 核验点7: BusinessException，返回自定义 bizCode + msg — GET /demo/exception/business */
    @GetMapping("/exception/business")
    public Result<Void> businessException() {
        throw new BusinessException(CommonResultCode.NOT_FOUND, "演示：目标资源不存在");
    }

    /** 核验点8: 未捕获异常，GlobalExceptionHandler 兜底返回 SYSTEM_ERROR — GET /demo/exception/system */
    @GetMapping("/exception/system")
    public Result<Void> systemException() {
        throw new RuntimeException("演示：未预期的系统异常");
    }

    /** 核验点9: Result.fail 不抛异常直接构造错误返回 — GET /demo/no-permission */
    @GetMapping("/no-permission")
    public Result<Void> noPermission() {
        return Result.fail(CommonResultCode.NO_PERMISSION);
    }
}
