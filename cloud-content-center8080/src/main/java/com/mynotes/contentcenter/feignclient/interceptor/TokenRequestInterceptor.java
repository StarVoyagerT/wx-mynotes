package com.mynotes.contentcenter.feignclient.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Author: 乔童
 * @Description: 请求拦截器，为每个请求添加X-Token实现内部裸奔
 * @Date: 2020/05/11 09:48
 * @Version: 1.0
 */
public class TokenRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        //1.获取到token
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String token = requestAttributes.getRequest().getHeader("X-Token");
        //2.把token传递
        template.header("X-Token",token);
    }

}
