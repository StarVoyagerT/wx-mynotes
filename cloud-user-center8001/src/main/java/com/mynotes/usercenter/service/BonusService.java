package com.mynotes.usercenter.service;

import com.mynotes.usercenter.domain.entity.user.BonusEventLog;
import com.mynotes.usercenter.domain.entity.user.User;

import java.util.List;

/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/05/13 19:05
 * @Version: 1.0
 */
public interface BonusService {
    List<BonusEventLog> selectByUser(Integer userId);

    User sign(Integer userId);
}
