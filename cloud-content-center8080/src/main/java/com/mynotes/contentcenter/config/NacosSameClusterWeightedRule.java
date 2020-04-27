package com.mynotes.contentcenter.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: 乔童
 * @Description: ribbon支持nacos权重规则
 * @Date: 2020/04/27 10:11
 * @Version: 1.0
 */
@Slf4j
public class NacosSameClusterWeightedRule extends AbstractLoadBalancerRule {

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
        //获取集群名称
        String clusterName = nacosDiscoveryProperties.getClusterName();
        Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
        //获取对应的版本
        String targetVersion = metadata.get("target-version");

        //获取名称服务
        NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();
        try {
            //最终被选择的instances
            List<Instance> instancesToBeChose;

            //1.根据服务名，获取目标服务的所有实例 A
            List<Instance> instances = namingService.selectInstances(name,true);

            //2.筛选相同集群的服务 B
            List<Instance> sameInstances = instances.stream()
                    .filter(instance -> Objects.equals(instance.getClusterName(), clusterName))
                    //筛选版本信息
                    .filter(instance -> Objects.equals(instance.getMetadata().get("version"),targetVersion))
                    .collect(Collectors.toList());

            //3.如果没有相同集群的服务，则返回A
            if(CollectionUtils.isEmpty(sameInstances))
            {
                instancesToBeChose=instances;
                log.warn("发生跨集群调用，serviceName：{}，clusterName：{}，instances：{}",
                        name,
                        clusterName,
                        instances
                );
            }
            //4.否则根据权重返回 B
            else {
                instancesToBeChose=sameInstances;
            }
            Instance instance = ExtendsBalancer.getHostByRandomWeightExtends(instancesToBeChose);
            log.info("选择的服务实例是：{}:{}，权重：{}，",instance.getIp(),instance.getPort(),instance.getWeight());
            return new NacosServer(instance);
        } catch (NacosException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
class ExtendsBalancer extends Balancer{
    public static Instance getHostByRandomWeightExtends(List<Instance> hosts) {
        return getHostByRandomWeight(hosts);
    }
}
