package com.mynotes.usercenter.rocketmq;

import com.mynotes.commons.domain.messaging.UserAddBonusMsgDTO;
import com.mynotes.usercenter.dao.user.UserMapper;
import com.mynotes.usercenter.domain.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/05/06 19:27
 * @Version: 1.0
 */
@Service
/**
 * topic参数 必须和生产者发送的destination一致
 * consumerGroup消费者组是在注解中的，必须写
 */
@RocketMQMessageListener(topic = "add-bonus",consumerGroup = "consumer-group")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AddBonusListener implements RocketMQListener<UserAddBonusMsgDTO> {

    private final UserMapper userMapper;


    @Override
    public void onMessage(UserAddBonusMsgDTO userAddBonusMsgDTO) {
        // 当收到消息的时候，执行的业务
        User user = new User();
        BeanUtils.copyProperties(userAddBonusMsgDTO,user);
        // 1.为用户加积分
        userMapper.updateByPrimaryKey(user);
        // 2.记录日志到bonus_event_log
    }
}
