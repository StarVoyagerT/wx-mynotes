package com.mynotes.usercenter.rocketmq;

import com.mynotes.commons.domain.messaging.UserAddBonusMsgDTO;
import com.mynotes.usercenter.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.stereotype.Service;

/**
 * @Author: 乔童
 * @Description: 添加积分消息消费者
 * @Date: 2020/05/08 09:37
 * @Version: 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddBonusStreamConsumer {

    private final UserService userService;

    @StreamListener(Sink.INPUT)
    public void receive(UserAddBonusMsgDTO bonusMsgDTO)
    {
        userService.changeBonus(bonusMsgDTO);
    }

    @StreamListener("errorChannel")
    public void error(Message<?> message)
    {
        ErrorMessage errorMessage= (ErrorMessage) message;
        log.warn("发生异常，异常信息：{}",errorMessage);
    }
}
