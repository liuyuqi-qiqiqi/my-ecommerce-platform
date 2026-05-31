package com.ecommerce.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.ecommerce.bff.client")
public class ShopBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopBffApplication.class, args);
    }
}
