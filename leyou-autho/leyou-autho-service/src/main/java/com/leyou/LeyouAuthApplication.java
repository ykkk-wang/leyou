package com.leyou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author wyk
 * @date 2020/7/28 23:49
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class LeyouAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(LeyouAuthApplication.class);
    }
}