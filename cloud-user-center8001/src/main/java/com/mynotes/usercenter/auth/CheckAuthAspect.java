package com.mynotes.usercenter.auth;

import com.mynotes.usercenter.security.CustomSecurityException;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;
@Aspect
@Component
public class CheckAuthAspect {

    @SneakyThrows
    @Around("@annotation(com.mynotes.usercenter.auth.CheckAuthorize)")
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
            if(!Objects.equals(role,value)&&!Objects.equals(role,"admin")){
                throw new CustomSecurityException("用户无权访问！");
            }
        } catch (Throwable e) {
            throw new CustomSecurityException("用户无权访问！");
        }
        return point.proceed();
    }
}
