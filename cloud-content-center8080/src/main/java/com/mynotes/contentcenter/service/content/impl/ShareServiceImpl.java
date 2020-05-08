package com.mynotes.contentcenter.service.content.impl;

import com.alibaba.fastjson.JSON;
import com.mynotes.commons.domain.messaging.UserAddBonusMsgDTO;
import com.mynotes.contentcenter.dao.content.RocketmqTransactionLogMapper;
import com.mynotes.contentcenter.dao.content.ShareMapper;
import com.mynotes.contentcenter.domain.dto.content.ShareAuditDTO;
import com.mynotes.contentcenter.domain.dto.content.ShareDTO;
import com.mynotes.contentcenter.domain.dto.user.UserDTO;
import com.mynotes.contentcenter.domain.entity.content.RocketmqTransactionLog;
import com.mynotes.contentcenter.domain.entity.content.Share;
import com.mynotes.contentcenter.domain.enums.AuditStatusEnum;
import com.mynotes.contentcenter.feignclient.UserCenterClient;
import com.mynotes.contentcenter.service.content.ShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @Author: 乔童
 * @Date: 2020/04/24 21:23
 * @Version: 1.0
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ShareServiceImpl implements ShareService {

    private final ShareMapper shareMapper;

    private final UserCenterClient userCenterClient;

    private final Source source;

    private final RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

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
        //检查share是否存在，不存在或当前的auditStatus != NOT_YET则抛出异常
        Share share = shareMapper.selectByPrimaryKey(id);
        if(share==null)
        {
            throw new IllegalArgumentException("参数非法，该分享不存在！");
        }else if(!share.getAuditStatus().equals(AuditStatusEnum.NOT_YET.toString()))
        {
            throw new IllegalArgumentException("参数非法，该分享不需要再审核！");
        }
        /*
            如果是PASS，为发布人添加积分，返回share
            异步执行，加积分的不是业务必须的，我发个帖子还要等待加完积分？显然不用
            如果是PASS，发送消息到MQ，让用户中心去消费，并为发布人增加积分
         */
        if(AuditStatusEnum.PASS.equals(auditDTO.getAuditStatusEnum()))
        {
            source.output().send(MessageBuilder.withPayload(
                    UserAddBonusMsgDTO.builder()
                            .userId(id)
                            .bonus(50)
                            .build()
                     )
                    .setHeader("share_id",id)
                    .setHeader(RocketMQHeaders.TRANSACTION_ID,UUID.randomUUID().toString())
                    .setHeader("dto", JSON.toJSONString(auditDTO))
                    .build());
        }
        //如果不是pass，不涉及用户增加积分，直接执行本地事务
        else{
            this.auditByIdInDB(id,auditDTO);
        }
        return shareMapper.selectByPrimaryKey(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditByIdInDB(Integer id,ShareAuditDTO auditDTO)
    {
        Share share = Share.builder()
                .id(id)
                .reason(auditDTO.getReason())
                .auditStatus(auditDTO.getAuditStatusEnum().toString())
                .build();
        /*
         * 注意updateByPrimaryKeySelective和updateByPrimaryKey的区别：
         * 前者会进行判断，如果传入的为null就不更新
         * 后者会传入的所有参数进行更新，包括null
         */
        int update = shareMapper.updateByPrimaryKeySelective(share);
        log.info("审核{}",update<1?"失败":"成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditByIdInRocketMQLog(Integer id,String transactionId,ShareAuditDTO auditDTO)
    {
        /*
         * 执行本地事务，更新审核
         */
        this.auditByIdInDB(id,auditDTO);
        RocketmqTransactionLog rocketmqTransactionLog = RocketmqTransactionLog.builder()
                .transactionId(transactionId)
                .log("审核...")
                .build();
        /*
         * insertSelective会将不为null的值插入字段中
         * insert会将全部值插入字段中，包括null，会覆盖默认值
         */
        rocketmqTransactionLogMapper.insertSelective(rocketmqTransactionLog);
    }
}
