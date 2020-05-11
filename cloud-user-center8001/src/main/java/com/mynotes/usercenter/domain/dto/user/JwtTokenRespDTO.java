package com.mynotes.usercenter.domain.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 乔童
 * @Description: JwtToken响应实体
 * @Date: 2020/05/10 09:49
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtTokenRespDTO {
    private String token;
    /**
     * 过期时间
     */
    private Long expirationTime;
}
