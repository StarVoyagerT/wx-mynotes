package com.mynotes.usercenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Description:
 * @Date: 2020/04/22 17:30
 * @Author: 乔童
 * @Version: 1.0
 */
@MapperScan("com.mynotes.usercenter.dao")
@SpringBootApplication
@EnableBinding(Sink.class)
public class UserCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserCenterApplication.class,args);
    }
}
