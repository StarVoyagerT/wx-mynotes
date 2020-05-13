package com.mynotes.usercenter.feignclient;

import com.mynotes.usercenter.domain.entity.content.Share;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * @Author: 乔童
 * @Description:内容中心客户端
 * @Date: 2020/05/13 19:31
 * @Version: 1.0
 */
@FeignClient(name = "content-center")
public interface ContentCenterClient {
    @GetMapping("/shares/my/contributes")
    List<Share> selectUserContributions(@RequestHeader(value = "X-Token", required = true) String token);
}
