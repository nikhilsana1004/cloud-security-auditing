package com.cloudaudit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class CloudAuditApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CloudAuditApplication.class, args);
    }
}
