package com.mynotes.usercenter.teststream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;

/**
 * @Author: 乔童
 * @Date: 2020/05/07 19:09
 * @Version: 1.0
 */
@Component
@Slf4j
public class TestStreamConsumer {
    @StreamListener(Sink.INPUT)
    public void onMessage(Object message)
    {
        log.info("通过stream收到了消息messageBody：{}",message);
    }
}
