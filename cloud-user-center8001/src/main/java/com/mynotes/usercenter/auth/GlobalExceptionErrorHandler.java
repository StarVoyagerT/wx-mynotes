package com.mynotes.usercenter.auth;

import com.mynotes.usercenter.security.CustomSecurityException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author: 乔童
 * @Description: 局部异常处理器
 * @Date: 2020/05/11 17:12
 * @Version: 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionErrorHandler {
    @ExceptionHandler(CustomSecurityException.class)
    public ResponseEntity<ErrorBody> error(CustomSecurityException e)
    {
        log.warn("发生SecurityException",e);
        return new ResponseEntity<>(ErrorBody.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .body("访问被拒绝！")
                .build(),
                HttpStatus.UNAUTHORIZED
        );
    }
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    private static class ErrorBody{
        private Integer status;
        private String body;
    }
}
