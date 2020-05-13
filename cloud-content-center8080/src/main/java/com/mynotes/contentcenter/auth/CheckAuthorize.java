package com.mynotes.contentcenter.auth;

import java.lang.annotation.*;

/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/05/11 10:25
 * @Version: 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckAuthorize {
    String value();
}
