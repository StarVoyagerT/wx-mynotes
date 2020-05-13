package com.mynotes.contentcenter.auth;

import com.mynotes.contentcenter.security.CustomSecurityException;
import com.mynotes.contentcenter.utils.JwtOperator;
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
 * @Description:检查登录状态注解
 * @Date: 2020/05/10 17:36
 * @Version: 1.0
 */
@Order(0)
@Aspect
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CheckLoginAspect {
    private final JwtOperator jwtOperator;

    @SneakyThrows
    @Around("@annotation(com.mynotes.contentcenter.auth.CheckLogin)")
    public Object checkLogin(ProceedingJoinPoint point)
    {
        //1.从请求上下文中获取header
        HttpServletRequest request=null;
        Claims claim = null;
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            request = requestAttributes.getRequest();
            String token = request.getHeader("X-Token");

            Boolean isValida = jwtOperator.validateToken(token);
            if(!isValida)
            {
                throw new CustomSecurityException("Token不合法/已过期！");
            }
            claim = jwtOperator.getClaimsFromToken(token);
        } catch (Throwable e) {
            throw new CustomSecurityException("Token不合法/已过期！");
        }
        request.setAttribute("id",claim.get("id"));
        request.setAttribute("wxNickname",claim.get("wxNickname"));
        request.setAttribute("role",claim.get("role"));
        return point.proceed();
    }
}
