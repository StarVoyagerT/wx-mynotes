package com.mynotes.usercenter.service.impl;

import com.mynotes.usercenter.dao.user.UserMapper;
import com.mynotes.usercenter.domain.entity.user.User;
import com.mynotes.usercenter.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/04/24 17:48
 * @Version: 1.0
 */
@Service
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;

    @Override
    public User selectById(Integer id) {
        return userMapper.selectByPrimaryKey(id);
    }
}
