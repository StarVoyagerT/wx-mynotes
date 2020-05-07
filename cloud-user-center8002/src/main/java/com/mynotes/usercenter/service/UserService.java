package com.mynotes.usercenter.service;

import com.mynotes.commons.domain.messaging.UserAddBonusMsgDTO;
import com.mynotes.usercenter.domain.entity.user.User;

/**
 * @Author: 乔童
 * @Description: 用户接口
 * @Date: 2020/04/24 17:45
 * @Version: 1.0
 */
public interface UserService {
    User selectById(Integer id);
    void addBonus(UserAddBonusMsgDTO userAddBonusMsgDTO);
}
