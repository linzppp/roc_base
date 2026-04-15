package org.roc.practice.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.roc.practice.constant.CommonResultCode;
import org.roc.practice.constant.IResultCode;
import org.roc.practice.exception.BaseException;
import org.roc.practice.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "org.roc.practice")
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
     * 受控业务异常（BusinessException / BaseException 子类）
     * HTTP 状态码由 IResultCode.getHttpStatus() 决定
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Result<?>> handleBase(BaseException e) {
        IResultCode rc = e.getResultCode();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.error(rc, e.getMessage()));
    }

    /**
     * 兜底：未预期异常，打完整日志，返回 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception on [{}]", request.getRequestURI(), e);
        return ResponseEntity.internalServerError()
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
        return ResponseEntity.badRequest()
                .body(Result.error(CommonResultCode.PARAM_ERROR, msg, errors));
    }
}
