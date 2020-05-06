package com.mynotes.contentcenter.service.content.impl;

import com.mynotes.contentcenter.dao.content.ShareMapper;
import com.mynotes.contentcenter.domain.dto.content.ShareAuditDTO;
import com.mynotes.contentcenter.domain.dto.content.ShareDTO;
import com.mynotes.commons.domain.messaging.UserAddBonusMsgDTO;
import com.mynotes.contentcenter.domain.dto.user.UserDTO;
import com.mynotes.contentcenter.domain.entity.content.Share;
import com.mynotes.contentcenter.domain.enums.AuditStatusEnum;
import com.mynotes.contentcenter.feignclient.UserCenterClient;
import com.mynotes.contentcenter.service.content.ShareService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: 乔童
 * @Date: 2020/04/24 21:23
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShareServiceImpl implements ShareService {

    private final ShareMapper shareMapper;

    private final UserCenterClient userCenterClient;

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public ShareDTO selectShareById(Integer id){
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

    @Override
    public Share auditById(Integer id, ShareAuditDTO auditDTO) {
        //1.检查share是否存在，不存在或当前的auditStatus != NOT_YET则抛出异常
        Share share = shareMapper.selectByPrimaryKey(id);
        if(share==null)
        {
            throw new IllegalArgumentException("参数非法，该分享不存在！");
        }else if(!share.getAuditStatus().equals(AuditStatusEnum.NOT_YET.toString()))
        {
            throw new IllegalArgumentException("参数非法，该分享不需要再审核！");
        }
        //2.设置审核状态为PASS/REJECT
        share.setAuditStatus(auditDTO.getAuditStatusEnum().toString());
        shareMapper.updateByPrimaryKey(share);
        /*3.如果是PASS，为发布人添加积分，返回share
            异步执行，加积分的不是业务必须的，我发个帖子还要等待加完积分？显然不用
            如果是PASS，发送消息到MQ，让用户中心去消费，并为发布人增加积分
         */
        rocketMQTemplate.convertAndSend("add-bonus",new UserAddBonusMsgDTO(id,50));

        return share;
    }
}
