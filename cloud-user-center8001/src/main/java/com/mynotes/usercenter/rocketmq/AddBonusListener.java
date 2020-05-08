/*
package com.mynotes.usercenter.rocketmq;

import com.mynotes.commons.domain.messaging.UserAddBonusMsgDTO;
import com.mynotes.usercenter.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

*/
/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/05/06 19:27
 * @Version: 1.0
 *//*

@Component
*/
/*
 * topic参数 必须和生产者发送的destination一致
 * consumerGroup消费者组是在注解中的，必须写
 *//*
@RocketMQMessageListener(topic = "add-bonus",consumerGroup = "consumer-group")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AddBonusListener implements RocketMQListener<UserAddBonusMsgDTO> {

    private final UserService userService;

    @Override
    public void onMessage(UserAddBonusMsgDTO message) {
        userService.addBonus(message);
    }
}*/
