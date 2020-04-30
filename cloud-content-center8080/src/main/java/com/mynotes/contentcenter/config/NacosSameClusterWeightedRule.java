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
        //最终被选中的实例
        Instance instanceBeChoose = null;
        //获取服务名称
        String name = loadBalancer.getName();
        //获取名称服务
        NamingService namingService = nacosDiscoveryProperties.namingServiceInstance();
        //获取集群名称
        String clusterName = nacosDiscoveryProperties.getClusterName();
        try {
            //1.获取所有指定服务的实例 A
            List<Instance> allInstances = namingService.getAllInstances(name,true);
            //2.筛选所有相应集群的实例 B
            List<Instance> clusterInstances = allInstances.stream()
                    .filter(instance -> Objects.equals(instance.getClusterName(), clusterName))
                    .collect(Collectors.toList());
            //3.判断是否为空 如果为空 根据A来返回一个实例
            if(CollectionUtils.isEmpty(clusterInstances))
            {
                log.warn("正在发生跨级群调用，服务：{}，实例列表：{}",name,allInstances);
                instanceBeChoose = ExtendsBalancer.getHostByRandomWeightExtends(allInstances);
            }
            //4.如果不为空，根据A来
            else{
                instanceBeChoose = ExtendsBalancer.getHostByRandomWeightExtends(clusterInstances);
            }
            return new NacosServer(instanceBeChoose);
        } catch (NacosException e) {
            log.info(e.toString());
            return null;
        }
    }
}
/**
 * 这是一个技巧，当源码的某个方法不是public的，可以通过内部类继承来简单包装该方法
 */
class ExtendsBalancer extends Balancer{
    public static Instance getHostByRandomWeightExtends(List<Instance> hosts) {
        return getHostByRandomWeight(hosts);
    }
}
