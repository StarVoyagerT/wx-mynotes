package com.mynotes.usercenter.security;

/**
 * @Author: 乔童
 * @Description: 安全异常
 * @Date: 2020/05/10 18:40
 * @Version: 1.0
 */
public class SecurityException extends RuntimeException{
    public SecurityException(String message) {
        super(message);
    }
}
