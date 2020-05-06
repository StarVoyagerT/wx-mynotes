package com.mynotes.contentcenter.feignclient.fallback;

import com.mynotes.contentcenter.domain.dto.user.UserDTO;
import com.mynotes.contentcenter.feignclient.UserCenterClient;
import org.springframework.stereotype.Component;

/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/05/03 11:51
 * @Version: 1.0
 */
@Component
public class UserCenterClientFallback implements UserCenterClient {
    @Override
    public UserDTO selectUserById(Integer id) {
        UserDTO userDTO=new UserDTO();
        userDTO.setWxNickname("默认用户");
        return userDTO;
    }
}
