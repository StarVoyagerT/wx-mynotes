package com.mynotes.contentcenter.auth;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @Author: 乔童
 * @Description: 授权切面
 * @Date: 2020/05/11 10:26
 * @Version: 1.0
 */
@Order(1)
@Aspect
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CheckAuthorizeAspect {
    @SneakyThrows
    @Around("@annotation(com.mynotes.contentcenter.auth.CheckAuthorize)")
    public Object authorize(ProceedingJoinPoint point) {
        try {
            //1.从登录的用户中获取role权限
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
            String role = (String) request.getAttribute("role");

            //2.获取当前方法签名
            MethodSignature signature = (MethodSignature) point.getSignature();
            Method method = signature.getMethod();

            //3.获取当前注解，从注解中获取参数
            CheckAuthorize annotation = method.getAnnotation(CheckAuthorize.class);
            String value=annotation.value();

            //4.将登录用户权限与注解参数进行对比，如果不对就直接抛异常拒绝用户继续操作
            if(!Objects.equals(role,value)){
                throw new SecurityException("用户无权访问！");
            }
        } catch (Throwable e) {
            throw new SecurityException("用户无权访问！");
        }
        return point.proceed();
    }
}
