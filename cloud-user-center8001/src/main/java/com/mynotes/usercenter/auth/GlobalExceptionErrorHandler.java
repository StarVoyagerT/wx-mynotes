package com.mynotes.usercenter.auth;

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
 * @Description: 局部异常处理
 * @Date: 2020/05/10 21:28
 * @Version: 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionErrorHandler {
    @ExceptionHandler
    public ResponseEntity<ErrorBody> error(SecurityException e)
    {
        log.warn("发生SecurityException异常",e);
        return new ResponseEntity<>(ErrorBody.builder()
            .body("用户不允许访问！")
            .Status(HttpStatus.UNAUTHORIZED.value())
            .build(), HttpStatus.UNAUTHORIZED);
    }
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class ErrorBody{
    private String body;
    private int Status;
}