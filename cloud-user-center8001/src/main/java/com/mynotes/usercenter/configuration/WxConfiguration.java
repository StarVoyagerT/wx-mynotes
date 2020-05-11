package com.mynotes.usercenter.configuration;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 乔童
 * @Description: 微信配置类
 * @Date: 2020/05/10 10:09
 * @Version: 1.0
 */
@Configuration
public class WxConfiguration {
    @Bean
    public WxMaConfig wxMaConfig()
    {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid("wx3d99546b8ccee786");
        config.setSecret("7228aef407682eb83d0e557b2a497541");
        return config;
    }

    @Bean
    public WxMaService wxMaService(WxMaConfig config)
    {
        WxMaServiceImpl service=new WxMaServiceImpl();
        service.setWxMaConfig(config);
        return service;
    }
}
