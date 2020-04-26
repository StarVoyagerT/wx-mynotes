package com.mynotes.contentcenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @Author: 乔童
 * @Description:
 * @Date: 2020/04/22 17:30
 * @Version: 1.0
 */
@MapperScan("com.mynotes.contentcenter.dao")
@SpringBootApplication
public class ContentCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentCenterApplication.class,args);
    }
    @Bean
    public RestTemplate getRestTemplate()
    {
        return new RestTemplate();
    }
}
