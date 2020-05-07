package com.mynotes.usercenter.service.impl;

import com.mynotes.commons.domain.messaging.UserAddBonusMsgDTO;
import com.mynotes.usercenter.dao.user.BonusEventLogMapper;
import com.mynotes.usercenter.dao.user.UserMapper;
import com.mynotes.usercenter.domain.entity.user.BonusEventLog;
import com.mynotes.usercenter.domain.entity.user.User;
import com.mynotes.usercenter.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/04/24 17:48
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    private final BonusEventLogMapper bonusEventLogMapper;

    @Override
    public User selectById(Integer id) {
        return userMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBonus(UserAddBonusMsgDTO msgDTO) {
        Integer bonus = msgDTO.getBonus();
        User user = userMapper.selectByPrimaryKey(msgDTO.getUserId());
        user.setBonus(user.getBonus() + bonus);

        //1.更新用户积分
        int update = userMapper.updateByPrimaryKeySelective(user);
        //2.插入更新日志
        int insert = bonusEventLogMapper.insertSelective(
                BonusEventLog.builder()
                        .userId(user.getId())
                        .createTime(new Date())
                        .value(msgDTO.getBonus())
                        .event("666666666")
                        .description("分享加积分")
                        .build()
        );
        log.info("用户添加积分{}",update==1&&insert==1?"成功":"失败");
    }
}
