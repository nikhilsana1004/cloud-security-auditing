package com.cloudaudit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cloudaudit")
public class CloudAuditConfig {
    private String databaseName;
    private String tableName;
    private String s3OutputLocation;
    private String awsRegion;
    private String bedrockModelId;
    private int maxRetries;
    private int waitTimeSeconds;
    private double temperature;
    private int maxTokens;
}
