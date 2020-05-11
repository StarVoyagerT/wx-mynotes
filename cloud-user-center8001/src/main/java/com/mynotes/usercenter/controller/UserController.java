package com.mynotes.usercenter.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.mynotes.commons.domain.entity.Result;
import com.mynotes.commons.enums.ResultEnum;
import com.mynotes.usercenter.auth.CheckLogin;
import com.mynotes.usercenter.domain.dto.user.JwtTokenRespDTO;
import com.mynotes.usercenter.domain.dto.user.LoginRespDTO;
import com.mynotes.usercenter.domain.dto.user.UserLoginRespDTO;
import com.mynotes.usercenter.domain.dto.user.UserRespDTO;
import com.mynotes.usercenter.domain.entity.user.User;
import com.mynotes.usercenter.service.UserService;
import com.mynotes.usercenter.utils.JwtOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

/**
 * @Author: 乔童
 * @Description: 用户控制器
 * @Date: 2020/04/24 17:49
 * @Version: 1.0
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class UserController {

    private final UserService userService;

    private final WxMaService wxMaService;

    private final JwtOperator jwtOperator;

    @GetMapping("/{id}")
    @ResponseBody
    @CheckLogin
    public User selectUserById(@PathVariable("id") Integer id) {
        User user = userService.selectById(id);
        Result<User> result;
        log.info("我被调用了");
        if (user != null) {
            result = new Result<>(ResultEnum.REQUEST_SUCCESS, user);
        } else {
            result = new Result<>(ResultEnum.REQUEST_FAIL, null);
        }
        return user;
    }

    @PostMapping("/login")
    public LoginRespDTO login(@RequestBody UserLoginRespDTO userloginRespDTO) throws WxErrorException {

        //微信小程序服务端校验是否已经登录的结果
        WxMaJscode2SessionResult result = wxMaService.getUserService()
                .getSessionInfo(userloginRespDTO.getCode());
        //微信的openId，用户在微信这里的唯一标识
        String openId = result.getOpenid();

        // 1.看用户是否注册，如果没有注册，就注册一下
        User user = userService.login(userloginRespDTO, openId);

        // 2.如果已经注册，颁发token
        HashMap<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("wxNickname", user.getWxNickname());
        userInfo.put("role", user.getRoles());

        String token = jwtOperator.generateToken(userInfo);
        log.info("用户{}登录成功，生成的token：{}，有效期到：{}", user.getWxNickname(), token, jwtOperator.getExpirationDateFromToken(token));

        UserRespDTO userRespDTO = new UserRespDTO();
        BeanUtils.copyProperties(user, userRespDTO);

        return LoginRespDTO.builder()
                .user(userRespDTO)
                .token(JwtTokenRespDTO.builder()
                        .token(token)
                        .expirationTime(jwtOperator.getExpirationDateFromToken(token).getTime())
                        .build())
                .build();
    }

}
