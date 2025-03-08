package com.maijishop.crmeb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Maijishop CRMEB主应用程序
 * 
 * @author maijishop
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableScheduling
public class MaijishopCrmebApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MaijishopCrmebApplication.class, args);
    }
} 