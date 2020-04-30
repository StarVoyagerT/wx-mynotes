package com.mynotes.contentcenter.service.content.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.mynotes.contentcenter.dao.content.ShareMapper;
import com.mynotes.contentcenter.domain.dto.content.ShareDTO;
import com.mynotes.contentcenter.domain.dto.user.UserDTO;
import com.mynotes.contentcenter.domain.entity.content.Share;
import com.mynotes.contentcenter.feign.client.UserCenterClient;
import com.mynotes.contentcenter.service.content.ShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    private final UserCenterClient userCenterClient;

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

        //通过远程接口调用获取用户中心的用户信息
        UserDTO userDTO = userCenterClient.selectUserById(id);
        //如果为null，说明不存在该用户
        if(userDTO==null)
        {
            return null;
        }
        shareDTO.setWxNickname(userDTO.getWxNickname());
        return shareDTO;
    }
}
