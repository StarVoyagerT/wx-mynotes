package com.mynotes.usercenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/04/22 17:30
 * @Version: 1.0
 */
@MapperScan("com.mynotes.usercenter.dao")
@SpringBootApplication
public class UserCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserCenterApplication.class,args);
    }
}
