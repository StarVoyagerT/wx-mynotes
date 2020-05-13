package com.mynotes.contentcenter.security;

/**
 * @Author: 乔童
 * @Description: 安全异常
 * @Date: 2020/05/10 18:40
 * @Version: 1.0
 */
public class CustomSecurityException extends RuntimeException{
    public CustomSecurityException(String message) {
        super(message);
    }
}
