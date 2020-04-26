package com.mynotes.commons.enums;

import lombok.Getter;

/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/04/24 18:01
 * @Version: 1.0
 */
@Getter
public enum ResultEnum {

    REQUEST_FAIL(400,"请求失败，未找到信息或参数不正确"),
    REQUEST_SUCCESS(200,"请求成功");

    private final Integer statusCode;

    private final String message;

    ResultEnum(Integer statusCode, String message)
    {
        this.statusCode=statusCode;
        this.message=message;
    }
}
