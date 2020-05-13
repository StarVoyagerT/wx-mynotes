package com.mynotes.contentcenter.controller;

import com.alibaba.nacos.api.exception.NacosException;
import com.github.pagehelper.PageInfo;
import com.mynotes.contentcenter.auth.CheckAuthorize;
import com.mynotes.contentcenter.auth.CheckLogin;
import com.mynotes.contentcenter.domain.dto.content.ShareDTO;
import com.mynotes.contentcenter.domain.entity.content.Share;
import com.mynotes.contentcenter.service.content.ShareService;
import com.mynotes.contentcenter.utils.JwtOperator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: 乔童
 * @Description: 内容控制器
 * @Date: 2020/04/24 21:21
 * @Version: 1.0
 */
@RestController
@RequestMapping("/shares")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ShareController {

    private final ShareService shareService;

    private final JwtOperator jwtOperator;

    @CheckLogin
    @GetMapping(value = "/{id}", produces = ("application/json;charset=utf-8"))
    public ShareDTO selectShareById(@PathVariable("id") Integer id) {
        ShareDTO shareDTO = null;
        try {
            shareDTO = shareService.selectShareById(id);
        } catch (NacosException e) {
            log.info("发生NacosException", e);
            e.printStackTrace();
        }
        log.info("请求返回值：{}", shareDTO);
        return shareDTO;
    }

    @CheckLogin
    @GetMapping("/my/contributes")
    public List<Share> selectUserContributions(@RequestHeader(value = "X-Token",required = true) String token)
    {
        Integer id = getUserIdById(token);
        return shareService.selectUserContributions(id);
    }

    @CheckLogin
    @GetMapping("/user")
    public List<Share> user(@RequestHeader(value = "X-Token",required = true) String token)
    {
        Integer id = getUserIdById(token);
        return shareService.selectExchangeSharesByUser(id);
    }

    /**
     * @param title    标题config.properties
     * @param pageNo   页码
     * @param pageSize 每页显示多少条
     * @return 分页信息
     */
    @GetMapping(value = "/q", produces = "application/json;charset=utf-8")
    public PageInfo<Share> q(
            @RequestParam(required = false) String title,
            @RequestParam(required = false, defaultValue = "1") Integer pageNo,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestHeader(value = "X-Token", required = false) String token
    ) {
        //注意点：pageSize必须做控制，不做控制万一用户给你传来个10W。。。。
        //每页最多显示30
        if (pageSize > 30) {
            pageSize = 30;
        }

        Integer userId = null;
        if (StringUtils.isNotBlank(token)) {
            Claims claims = jwtOperator.getClaimsFromToken(token);
            userId = (Integer) claims.get("id");
        }

        PageInfo<Share> q = shareService.q(title, pageNo, pageSize, userId);
        return q;
    }

    @CheckLogin
    @CheckAuthorize("user")
    @GetMapping("/exchange/{id}")
    public Share exchangeShareById(@PathVariable("id") Integer id) {
        return shareService.exchangeShareById(id);
    }

    private Integer getUserIdById(@RequestHeader(value = "X-Token", required = true) String token) {
        Claims claims = jwtOperator.getClaimsFromToken(token);
        return (Integer) claims.get("id");
    }
}
