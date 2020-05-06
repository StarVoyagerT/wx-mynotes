package com.mynotes.commons.domain.entity;

import com.mynotes.commons.enums.ResultEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/04/24 17:51
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result<T> {
    private Integer statusCode;
    private String message;
    private T data;

    public Result(ResultEnum resultEnum, T data) {
        this.statusCode=resultEnum.getStatusCode();
        this.message=resultEnum.getMessage();
        this.data=data;
    }
}
