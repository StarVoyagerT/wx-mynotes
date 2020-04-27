package com.mynotes.contentcenter.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @Author: 乔童
 * @Description: ribbon支持nacos权重规则
 * @Date: 2020/04/27 10:11
 * @Version: 1.0
 */
@Slf4j
public class NacosWeightedRule extends AbstractLoadBalancerRule {

    @Resource
    private NacosDiscoveryProperties nacosDiscoveryProperties;

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        //读取配置并初始化，不需要它所以留空，很多实现规则也是留空的
    }

    @Override
    public Server choose(Object key) {
        BaseLoadBalancer loadBalancer = (BaseLoadBalancer) this.getLoadBalancer();

        //获取提供者服务名称
        String name = loadBalancer.getName();

        //获取名称服务
        NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();
        try {
            //根据服务名，获取一个实例，它是基于权重的负载均衡策略
            Instance instance = namingService.selectOneHealthyInstance(name);
            log.info("选择的服务实例是：{}:{}，权重：{}，",instance.getIp(),instance.getPort(),instance.getWeight());
            return new NacosServer(instance);
        } catch (NacosException e) {
            return null;
        }
    }
}
