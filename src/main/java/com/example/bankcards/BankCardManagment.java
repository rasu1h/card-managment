package com.example.bankcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
public class BankCardManagment {
    public static void main(String[] args) {
        SpringApplication.run(BankCardManagment.class, args);
    }
}

