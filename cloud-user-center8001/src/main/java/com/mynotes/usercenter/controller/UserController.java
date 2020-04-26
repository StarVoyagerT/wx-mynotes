package com.mynotes.usercenter.controller;

import com.mynotes.commons.entity.Result;
import com.mynotes.commons.enums.ResultEnum;
import com.mynotes.usercenter.dao.user.UserMapper;
import com.mynotes.usercenter.domain.entity.user.User;
import com.mynotes.usercenter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @Author: 乔童
 * @Description: 用户控制器
 * @Date: 2020/04/24 17:49
 * @Version: 1.0
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {

    private final UserMapper userMapper;

    @Resource
    private UserService userService;

    @GetMapping("/{id}")
    @ResponseBody
    public Result<User> selectUserById(@PathVariable("id") Integer id)
    {

        User user = userService.selectById(id);
        Result<User> result;

        if(user!=null)
        {
            result=new Result<>(ResultEnum.REQUEST_SUCCESS,user);
        }else {
            result=new Result<>(ResultEnum.REQUEST_FAIL,null);
        }
        return result;
    }
}
