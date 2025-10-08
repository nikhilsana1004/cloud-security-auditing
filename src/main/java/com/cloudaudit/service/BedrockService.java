package com.cloudaudit.service;

import com.cloudaudit.config.CloudAuditConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class BedrockService {
    
    private final BedrockRuntimeClient bedrockClient;
    private final CloudAuditConfig config;
    private final ObjectMapper objectMapper;
    
    public BedrockService(BedrockRuntimeClient bedrockClient, 
                         CloudAuditConfig config,
                         ObjectMapper objectMapper) {
        this.bedrockClient = bedrockClient;
        this.config = config;
        this.objectMapper = objectMapper;
    }
    
    @Retryable(
        retryFor = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public String invokeClaude(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("prompt", "\n\nHuman: " + prompt + "\n\nAssistant:");
            requestBody.put("temperature", config.getTemperature());
            requestBody.put("top_p", 0.7);
            requestBody.put("top_k", 40);
            requestBody.put("max_tokens_to_sample", config.getMaxTokens());
            
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            
            InvokeModelRequest request = InvokeModelRequest.builder()
                    .modelId(config.getBedrockModelId())
                    .body(SdkBytes.fromUtf8String(jsonBody))
                    .build();
            
            InvokeModelResponse response = bedrockClient.invokeModel(request);
            String responseBody = response.body().asUtf8String();
            
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("completion").asText().trim();
        } catch (Exception e) {
            log.error("Error invoking Bedrock model", e);
            throw new RuntimeException("Failed to invoke LLM", e);
        }
    }
}
