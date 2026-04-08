package org.roc.practice.exception;

import lombok.extern.slf4j.Slf4j;
import org.roc.practice.core.constant.ResultCode;
import org.roc.practice.core.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusiness(BusinessException e){
        ResultCode code = e.getResultCode();
        Result<?> body = e.getCustomMsg() != null
                ? Result.error(code, e.getCustomMsg())
                : Result.error(code);
        return ResponseEntity.status(code.getHttpStatus()).body(body);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Result<?>> handleBase(BaseException e){
        ResultCode code = e.getResultCode();
        Result<?> body = Result.error(code);
        return ResponseEntity.status(code.getHttpStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<?>> handleValidation(MethodArgumentNotValidException e){
        ResultCode code = ResultCode.PARAM_ERROR;
        String msg = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField()+ ": " + fe.getDefaultMessage())
                .collect(Collectors.joining());
        Result<?> body = Result.error(code, msg);
        return ResponseEntity.status(code.getHttpStatus()).body(body);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleUnknown(Exception e) {
        log.error("Unhandled exception", e);
        ResultCode code = ResultCode.SYSTEM_ERROR;
        Result<?> body = Result.error(code);
        return ResponseEntity.status(code.getHttpStatus()).body(body);
    }
}
