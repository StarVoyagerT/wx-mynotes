package com.mynotes.contentcenter.auth;

import java.lang.annotation.*;

/**
 * @Author: 乔童
 * @Description: 检查登录状态注解
 * @Date: 2020/05/10 18:34
 * @Version: 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckLogin {
}
