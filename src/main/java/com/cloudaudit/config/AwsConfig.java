package com.cloudaudit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;

@Configuration
public class AwsConfig {
    
    private final CloudAuditConfig config;
    
    public AwsConfig(CloudAuditConfig config) {
        this.config = config;
    }
    
    @Bean
    public AthenaClient athenaClient() {
        return AthenaClient.builder()
                .region(Region.of(config.getAwsRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
    
    @Bean
    public BedrockRuntimeClient bedrockClient() {
        return BedrockRuntimeClient.builder()
                .region(Region.of(config.getAwsRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
