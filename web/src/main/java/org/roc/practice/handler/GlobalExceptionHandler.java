package org.roc.practice.handler;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.roc.practice.constant.CommonResultCode;
import org.roc.practice.constant.IResultCode;
import org.roc.practice.exception.BaseException;
import org.roc.practice.exception.BusinessException;
import org.roc.practice.exception.RocSystemException;
import org.roc.practice.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
/**
 * 全局异常处理器。
 *
 * <h3>HTTP 状态码策略</h3>
 * 所有异常响应均返回 HTTP 200，通过 {@code bizCode} 字段区分业务成功与失败。
 * <ul>
 *   <li>设计动机：避免与 Sentinel 熔断器基于 HTTP 错误率的统计冲突；简化 Gateway 层配置。</li>
 *   <li>这是国内中大型互联网系统（阿里、字节等）的主流做法：HTTP 层只传输，
 *       业务状态由 bizCode 表达，监控告警基于 bizCode 而非 HTTP Status。</li>
 * </ul>
 *
 * <h3>覆盖范围</h3>
 * 不限制 basePackages，覆盖宿主应用所有包下的 Controller。
 * Actuator 有独立的 {@code BasicErrorController} 错误处理链，不受本 Handler 影响。
 * {@code basePackages} 限制的是"哪些 Controller 的异常被拦截"，不是过滤异常类的来源包，
 * 框架作为被引入方必须支持宿主应用的任意包结构。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @RequestBody @Validated 校验失败
     * msg = 第一个字段的错误信息（用户友好）
     * data = 所有字段错误的结构化 Map（便于前端精确定位）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Map<String, String>>> handleValidation(MethodArgumentNotValidException e) {
        return buildValidationResponse(e);
    }

    /**
     * form 表单绑定失败（@ModelAttribute 等场景）
     * msg = 第一个字段的错误信息（用户友好）
     * data = 所有字段错误的结构化 Map（便于前端精确定位）
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Map<String, String>>> handleBind(BindException e) {
        return buildValidationResponse(e);
    }

    /**
     * 请求体解析失败, 存在四种可能:
     * JSON格式匹配失败, 要求字段类型与传入类型不匹配, 枚举值/日期格式非法, 请求体为空但是 contentType声明为 Json
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<?>> handleNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.ok().body(Result.error(CommonResultCode.NOT_READABLE));
    }

    /**
     * 受控业务异常（BusinessException / BaseException 子类）
     * 通常非系统错误，是可预期的业务错误；需要‘用户’配合操作系统
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusiness(BaseException e) {
        IResultCode rc = e.getResultCode();
        return ResponseEntity.ok().body(Result.error(rc, e.getMessage()));
    }

    /**
     * 非受控系统异常
     * 用户正常行为但导致系统非预期逻辑 ，致使逻辑无法执行/判断异常
     * 需要研发介入调查
     */
    @ExceptionHandler(RocSystemException.class)
    public ResponseEntity<Result<?>> handleRocSystem(BaseException e, HttpServletRequest request) {
        IResultCode rc = e.getResultCode();
        log.error("RocSystemException:{}, happens on {}", e.getMessage(), request.getRequestURI());
        return ResponseEntity.ok().body(Result.error(rc, e.getMessage()));
    }

    /**
     * 兜底：未预期异常，打完整日志，返回 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception on [{}]", request.getRequestURI(), e);
        return ResponseEntity.ok()
                .body(Result.error(CommonResultCode.SYSTEM_ERROR));
    }

    private ResponseEntity<Result<Map<String, String>>> buildValidationResponse(BindException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        // msg 取第一个错误，给用户友好提示
        String msg = fieldErrors.isEmpty()
                ? CommonResultCode.PARAM_ERROR.getMessage()
                : fieldErrors.get(0).getDefaultMessage();
        // data 包含所有字段错误，同一字段取第一条
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : fieldErrors) {
            errors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.ok()
                .body(Result.error(CommonResultCode.PARAM_ERROR, msg, errors));
    }
}
