package com.mynotes.contentcenter.service.content;

import com.alibaba.nacos.api.exception.NacosException;
import com.mynotes.contentcenter.domain.dto.content.ShareAuditDTO;
import com.mynotes.contentcenter.domain.dto.content.ShareDTO;
import com.mynotes.contentcenter.domain.entity.content.Share;

/**
 * @Author: 乔童
 * @Description: 分享接口
 * @Date: 2020/04/24 21:22
 * @Version: 1.0
 */
public interface ShareService {
    ShareDTO selectShareById(Integer id) throws NacosException;

    Share auditById(Integer id, ShareAuditDTO auditDTO);
}
