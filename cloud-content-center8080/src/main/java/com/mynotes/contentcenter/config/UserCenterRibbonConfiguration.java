package com.mynotes.contentcenter.config;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.Configuration;
import ribbonconfig.RibbonConfiguration;

/**
 * @Author: 乔童
 * @Description: 用户中心服务负载均衡配置
 * @Date: 2020/04/26 21:36
 * @Version: 1.0
 */
@Configuration
@RibbonClient(name="user-center",configuration = RibbonConfiguration.class)
public class UserCenterRibbonConfiguration {
}
