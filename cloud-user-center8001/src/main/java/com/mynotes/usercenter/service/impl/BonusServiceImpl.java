package com.mynotes.usercenter.service.impl;

import com.mynotes.usercenter.dao.user.BonusEventLogMapper;
import com.mynotes.usercenter.dao.user.UserMapper;
import com.mynotes.usercenter.domain.entity.user.BonusEventLog;
import com.mynotes.usercenter.domain.entity.user.User;
import com.mynotes.usercenter.service.BonusService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/05/13 19:06
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BonusServiceImpl implements BonusService {
    private final BonusEventLogMapper bonusEventLogMapper;
    private final UserMapper userMapper;
    @Override
    public List<BonusEventLog> selectByUser(Integer userId) {
        return bonusEventLogMapper.select(BonusEventLog.builder()
                .userId(userId)
                .build());
    }

    @Override
    @Transactional
    public User sign(Integer userId) {
        List<BonusEventLog> bonusEventLogs = bonusEventLogMapper.select(BonusEventLog.builder()
                .userId(userId)
                .event("SING")
                .build());
        /**
         * 获取最后一次签到记录信息
         */
        BonusEventLog bonusEventLog = bonusEventLogs.get(bonusEventLogs.size()-1);
        long afterTime =System.currentTimeMillis() - bonusEventLog.getCreateTime().getTime();

        User user = userMapper.selectByPrimaryKey(userId);
        //简单判断一下，如果签到不超过24小时就直接返回
        if(afterTime<(1000*60*60*24))
        {
            return user;
        }

        user.setBonus(user.getBonus()+50);
        userMapper.updateByPrimaryKey(user);
        bonusEventLogMapper.insertSelective(BonusEventLog.builder()
                .userId(userId)
                .createTime(new Date())
                .description("签到加积分")
                .event("SING")
                .build());
        return user;
    }
}
