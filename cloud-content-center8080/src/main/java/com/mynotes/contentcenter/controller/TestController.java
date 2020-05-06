package com.mynotes.contentcenter.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: 乔童
 * @Date: 2020/05/01 15:40
 * @Version: 1.0
 */
@RestController
@Slf4j
public class TestController {
    @GetMapping("/test-hot")
    @SentinelResource("hot")
    public String testHot(@RequestParam(required = false) String a,@RequestParam(required = false) String b)
    {
        return a+b;
    }

    @GetMapping("/test-sentinel")
    @SentinelResource(value = "test-sentinel",blockHandler = "block",fallback = "fallback")
    public String testSentinelResource(@RequestParam(required = false) String a)
    {
        if(StringUtils.isBlank(a))
        {
            throw new IllegalArgumentException("a cannot be blank");
        }
        return a;
    }
    public String block(String a, BlockException e)
    {
      log.warn("限流或者降级了 block",e);
      return "限流或者降级了 block";
    }
    public String fallback(String a, Throwable e)
    {
        log.warn("限流或者降级了 fallback",e);
        return "限流或者降级了 fallback";
    }
}
