package com.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ClothesShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClothesShopApplication.class, args);
    }
}
