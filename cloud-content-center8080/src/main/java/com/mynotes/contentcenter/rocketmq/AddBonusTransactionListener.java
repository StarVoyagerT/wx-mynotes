package com.mynotes.contentcenter.rocketmq;

import com.mynotes.contentcenter.dao.content.RocketmqTransactionLogMapper;
import com.mynotes.contentcenter.domain.dto.content.ShareAuditDTO;
import com.mynotes.contentcenter.domain.entity.content.RocketmqTransactionLog;
import com.mynotes.contentcenter.service.content.ShareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.Objects;

/**
 * @Author: 乔童
 * @Description: 添加积分MQ监听器
 * @Date: 2020/05/07 09:41
 * @Version: 1.0
 */
@RocketMQTransactionListener(txProducerGroup = "tx-add-bonus-group")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AddBonusTransactionListener implements RocketMQLocalTransactionListener {

    private final ShareService shareService;
    private final RocketmqTransactionLogMapper rocketmqTransactionLogMapper;

    /**
     * 执行本地事务，然后执行第4步向MQ发送第二次确认
     * @param message
     * @param arg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object arg) {
        MessageHeaders headers = message.getHeaders();
        String transactionId = (String) headers.get(RocketMQHeaders.TRANSACTION_ID);
        Integer shareId = Integer.parseInt((String) Objects.requireNonNull(headers.get("share_id")));
        try {
            //执行本地事务，执行成功后向TransactionLog表中插入记录，之后根据TranscationLog表消息回查
            shareService.auditByIdInRocketMQLog(shareId,transactionId, (ShareAuditDTO) arg);
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            log.info("本地事务执行失败，异常信息：{}",e.toString());
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }
    /**
     * 第5步的消息回查，当MQ没有收到第四步的第二次确认时，会通过该方法进行消息回查
     * @param message
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        MessageHeaders headers = message.getHeaders();
        String transactionId = (String) headers.get(RocketMQHeaders.TRANSACTION_ID);
        //本地事务执行之后的记录，如果该记录为null，说明事务执行失败
        RocketmqTransactionLog rocketmqTransactionLog = rocketmqTransactionLogMapper.selectOne(
                RocketmqTransactionLog.builder()
                        .transactionId(transactionId)
                        .build()
        );
        if (rocketmqTransactionLog != null) {
            return RocketMQLocalTransactionState.COMMIT;
        }
        return RocketMQLocalTransactionState.ROLLBACK;
    }
}
