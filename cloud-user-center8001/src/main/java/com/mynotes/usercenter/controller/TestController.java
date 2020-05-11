package com.mynotes.usercenter.controller;/*
package com.mynotes.usercenter.controller;

import com.mynotes.usercenter.dao.user.UserMapper;
import com.mynotes.usercenter.domain.entity.user.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

*/

import com.mynotes.usercenter.utils.JwtOperator;
import com.mynotes.usercenter.dao.user.UserMapper;
import com.mynotes.usercenter.domain.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * @Author: 乔童
 * @Description: 测试控制器
 * @Date: 2020/04/24 16:32
 * @Version: 1.0
 */
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TestController {

    private final JwtOperator jwtOperator;

    @Resource
    private UserMapper userMapper;

    @ResponseBody
    @GetMapping
    public List<User> testUserSelectAll()
    {
        return userMapper.selectAll();
    }

    @GetMapping("/1")
    @ResponseBody
    public User testUserSelectById()
    {
        return userMapper.selectByPrimaryKey(1);
    }


    @GetMapping("/get-token")
    public String loginTest()
    {
        HashMap<String, Object> userInfo = new HashMap<>();
        userInfo.put("id","1");
        userInfo.put("wxNickname","heheda");
        userInfo.put("role","user");
        return jwtOperator.generateToken(userInfo);
    }
}
