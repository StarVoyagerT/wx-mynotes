package com.mynotes.contentcenter.feignclient.fallbackfactory;

import com.mynotes.commons.domain.messaging.UserAddBonusMsgDTO;
import com.mynotes.contentcenter.domain.dto.user.UserDTO;
import com.mynotes.contentcenter.feignclient.UserCenterClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author: 乔童
 * @Date: 2020/05/03 16:03
 * @Version: 1.0
 */
@Slf4j
@Component
public class UserCenterClientFallbackFactory implements FallbackFactory<UserCenterClient> {
    @Override
    public UserCenterClient create(Throwable cause) {
        return new UserCenterClient() {
            @Override
            public UserDTO selectUserById(Integer id) {
                log.info("远程调用被限流/降级了",cause);
                UserDTO userDTO = new UserDTO();
                userDTO.setWxNickname("默认用户");
                return userDTO;
            }

            @Override
            public UserDTO changeBonus(UserAddBonusMsgDTO build) {
                log.info("远程调用被限流/降级了",cause);
                return null;
            }
        };
    }
}
