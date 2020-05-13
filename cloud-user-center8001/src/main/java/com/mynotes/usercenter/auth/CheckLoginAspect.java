package com.mynotes.usercenter.auth;

import com.mynotes.usercenter.security.CustomSecurityException;
import com.mynotes.usercenter.utils.JwtOperator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: 乔童
 * @Description: 检查登录状态切面
 * @Date: 2020/05/11 16:53
 * @Version: 1.0
 */
@Component
@Aspect
@Order(0)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CheckLoginAspect {

    private final JwtOperator jwtOperator;

    @SneakyThrows
    @Around("@annotation(com.mynotes.usercenter.auth.CheckLogin)")
    public Object checkLogin(ProceedingJoinPoint point)
    {
        try {
            //1.获取RequestContext中获取当前请求
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();

            //2.从头信息中获取token
            String token = request.getHeader("X-Token");

            //3.校验token的合法/过期
            Boolean isValida = jwtOperator.validateToken(token);

            //4.如果token不合法/过期，抛出自定义异常
            if(!isValida)
            {
                throw new CustomSecurityException("Token过期/不合法！");
            }
            //5.如果合法，根据token获取claim，给当前请求用户添加用户信息
            Claims claim = jwtOperator.getClaimsFromToken(token);
            request.setAttribute("id",claim.get("id"));
            request.setAttribute("wxNickname",claim.get("wxNickname"));
            request.setAttribute("role",claim.get("role"));
        } catch (Throwable e) {
            throw new CustomSecurityException("Token过期/不合法！");
        }
        return point.proceed();
    }
}
