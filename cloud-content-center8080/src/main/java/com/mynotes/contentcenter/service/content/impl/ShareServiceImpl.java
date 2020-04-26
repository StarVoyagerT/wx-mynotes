package com.mynotes.contentcenter.service.content.impl;

import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.nacos.api.exception.NacosException;
import com.mynotes.contentcenter.dao.content.ShareMapper;
import com.mynotes.contentcenter.domain.dto.content.ShareDTO;
import com.mynotes.contentcenter.domain.dto.user.UserDTO;
import com.mynotes.contentcenter.domain.entity.content.Share;
import com.mynotes.contentcenter.service.content.ShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * @Author: 乔童
 * @Date: 2020/04/24 21:23
 * @Version: 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareServiceImpl implements ShareService {

    private final ShareMapper shareMapper;

    private final RestTemplate restTemplate;

    private final NacosServiceDiscovery serviceDiscovery;
    @Override
    public ShareDTO selectShareById(Integer id) throws NacosException {
        Share share = shareMapper.selectByPrimaryKey(id);
        //如果为null，说明不存在该分享
        if(share==null)
        {
            return null;
        }
        ShareDTO shareDTO = new ShareDTO();
        BeanUtils.copyProperties(share,shareDTO);
        //获取用户中心所有实例的信息
        List<ServiceInstance> userCenter = serviceDiscovery.getInstances("user-center");
        List<String> targetURLCollect = userCenter.stream()
                .map(instance -> instance.getUri().toString() + "/users/" + id)
                .collect(Collectors.toList());
        //模拟随机负载均衡策略
        int i = ThreadLocalRandom.current().nextInt(targetURLCollect.size());

        log.info("请求URL：{}，参数：{}",targetURLCollect.get(i),id);

        //通过远程接口调用获取用户中心的用户信息
        UserDTO userDTO = restTemplate.getForObject(targetURLCollect.get(i),UserDTO.class);
        //如果为null，说明不存在该用户
        if(userDTO==null)
        {
            return null;
        }
        shareDTO.setWxNickname(userDTO.getWxNickname());
        return shareDTO;
    }
}
