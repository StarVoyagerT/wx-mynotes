package com.mynotes.contentcenter.domain.dto.content;

import com.mynotes.contentcenter.domain.enums.AuditStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/05/03 21:33
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareAuditDTO {
    private AuditStatusEnum auditStatusEnum;
    /**
     * 原因
     */
    private String reason;
}
