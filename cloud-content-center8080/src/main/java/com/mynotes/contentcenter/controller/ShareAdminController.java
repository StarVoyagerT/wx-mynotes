package com.mynotes.contentcenter.controller;

import com.mynotes.contentcenter.domain.dto.content.ShareAuditDTO;
import com.mynotes.contentcenter.domain.entity.content.Share;
import com.mynotes.contentcenter.service.content.ShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: 乔童
 * @Description: 管理员控制器
 * @Date: 2020/05/03 21:27
 * @Version: 1.0
 */
@RestController
@RequestMapping("/admin/shares")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ShareAdminController
{
    private final ShareService shareService;
    @PutMapping("/audit/{id}")
    public Share auditById(@PathVariable Integer id,@RequestBody ShareAuditDTO auditDTO)
    {
        log.info("审核用户ID：{},审核状态：{}",id,auditDTO);
        Share share = shareService.auditById(id, auditDTO);
        log.info("审核结果：{}",share.getAuditStatus());
        return share;
    }
}
