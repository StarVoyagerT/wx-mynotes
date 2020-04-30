package com.mynotes.contentcenter.feign.client;

import com.mynotes.contentcenter.domain.dto.user.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: 乔童
 * @Description: 用户中心接口
 * @Date: 2020/04/27 20:51
 * @Version: 1.0
 */
@FeignClient(value = "user-center")
@RequestMapping("/users")
public interface UserCenterClient {
    @GetMapping("/{id}")
    UserDTO selectUserById(@PathVariable("id") Integer id);
}
