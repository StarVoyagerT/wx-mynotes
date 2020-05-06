package com.mynotes.contentcenter.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/05/03 21:38
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum AuditStatusEnum {
    /**
     * 待审核
     */
    NOT_YET(0),
    /**
     * 审核通过
     */
    PASS(1),
    /**
     * 不通过
     */
    REJECT(2);

    Integer code;
}
