package com.mynotes.commons.domain.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 乔童
 * @Description: 用户增加积分消息实体
 * @Date: 2020/05/06 16:20
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAddBonusMsgDTO {
    private Integer userId;
    /**
     * 积分
     */
    private Integer bonus;
}
