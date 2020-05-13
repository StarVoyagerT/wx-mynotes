package com.mynotes.contentcenter.feignclient;

import com.mynotes.commons.domain.messaging.UserAddBonusMsgDTO;
import com.mynotes.contentcenter.domain.dto.user.UserDTO;
import com.mynotes.contentcenter.feignclient.fallbackfactory.UserCenterClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author: 乔童
 * @Description: 用户中心接口
 * @Date: 2020/04/27 20:51
 * @Version: 1.0
 */
@FeignClient(value = "user-center",
            fallbackFactory = UserCenterClientFallbackFactory.class
)
public interface UserCenterClient{
    @GetMapping(value = "/users/{id}")
    UserDTO selectUserById(@PathVariable("id") Integer id);

    @GetMapping(value = "/users/change-bonus")
    UserDTO changeBonus(UserAddBonusMsgDTO build);
}
