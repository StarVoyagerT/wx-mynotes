package com.mynotes.contentcenter.controller;

import com.alibaba.nacos.api.exception.NacosException;
import com.mynotes.commons.domain.entity.Result;
import com.mynotes.commons.enums.ResultEnum;
import com.mynotes.contentcenter.auth.CheckLogin;
import com.mynotes.contentcenter.domain.dto.content.ShareDTO;
import com.mynotes.contentcenter.service.content.ShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @CheckLogin
    @GetMapping(value = "/{id}",produces = ("application/json;charset=utf-8"))
    public Result<ShareDTO> selectShareById(@PathVariable("id") Integer id)
    {
        ShareDTO shareDTO = null;
        try {
            shareDTO = shareService.selectShareById(id);
        } catch (NacosException e) {
            log.info("发生NacosException",e);
            e.printStackTrace();
        }
        Result<ShareDTO> result;
        if(shareDTO!=null)
        {
            result=new Result<>(ResultEnum.REQUEST_SUCCESS,shareDTO);
        }else {
            result=new Result<>(ResultEnum.REQUEST_FAIL,null);
        }
        log.info("请求返回值：{}",result);
        return result;
    }
}
