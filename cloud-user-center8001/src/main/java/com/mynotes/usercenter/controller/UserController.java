package com.mynotes.usercenter.controller;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.mynotes.commons.domain.entity.Result;
import com.mynotes.commons.domain.messaging.UserAddBonusMsgDTO;
import com.mynotes.commons.enums.ResultEnum;
import com.mynotes.usercenter.auth.CheckLogin;
import com.mynotes.usercenter.domain.dto.user.JwtTokenRespDTO;
import com.mynotes.usercenter.domain.dto.user.LoginRespDTO;
import com.mynotes.usercenter.domain.dto.user.UserLoginRespDTO;
import com.mynotes.usercenter.domain.dto.user.UserRespDTO;
import com.mynotes.usercenter.domain.entity.content.Share;
import com.mynotes.usercenter.domain.entity.user.BonusEventLog;
import com.mynotes.usercenter.domain.entity.user.User;
import com.mynotes.usercenter.feignclient.ContentCenterClient;
import com.mynotes.usercenter.service.BonusService;
import com.mynotes.usercenter.service.UserService;
import com.mynotes.usercenter.utils.JwtOperator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

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

    private final BonusService bonusService;

    private final WxMaService wxMaService;

    private final JwtOperator jwtOperator;

    private final ContentCenterClient contentCenterClient;

    @GetMapping("/{id}")
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

    @GetMapping("/me")
    @CheckLogin
    public User me(HttpServletRequest request)
    {
        Integer id = (Integer) request.getAttribute("id");
        Object code = request.getAttribute("code");
        //根据id从数据库查询user
        User user = userService.selectById(id);
        //从请求中获取token
        String token = request.getHeader("X-Token");
        return user;
    }

    @GetMapping("/bonus-logs")
    public List<BonusEventLog> checkBonusLog(@RequestHeader("X-Token") String token)
    {
        Claims claims = jwtOperator.getClaimsFromToken(token);
        Integer userId = (Integer) claims.get("id");
        return bonusService.selectByUser(userId);
    }

    @GetMapping("/contributions")
    public List<Share> selectContributesByUser(@RequestHeader("X-Token")String token)
    {
        return contentCenterClient.selectUserContributions(token);
    }

    @CheckLogin
    @GetMapping("/sign")
    public User sign(HttpServletRequest request)
    {
        Integer userId = (Integer) request.getAttribute("id");
        return bonusService.sign(userId);
    }

    @GetMapping("/change-bonus")
    public User changeBonus(@RequestBody UserAddBonusMsgDTO userAddBonusMsgDTO)
    {
        return userService.changeBonus(userAddBonusMsgDTO);
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
