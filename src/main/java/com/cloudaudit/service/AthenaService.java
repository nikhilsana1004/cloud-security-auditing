package com.cloudaudit.service;

import com.cloudaudit.config.CloudAuditConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AthenaService {
    
    private final AthenaClient athenaClient;
    private final CloudAuditConfig config;
    
    public AthenaService(AthenaClient athenaClient, CloudAuditConfig config) {
        this.athenaClient = athenaClient;
        this.config = config;
    }
    
    @Retryable(
        retryFor = {AthenaException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public String executeQuery(String query) {
        try {
            StartQueryExecutionRequest request = StartQueryExecutionRequest.builder()
                    .queryString(query)
                    .queryExecutionContext(QueryExecutionContext.builder()
                            .database(config.getDatabaseName())
                            .build())
                    .resultConfiguration(ResultConfiguration.builder()
                            .outputLocation(config.getS3OutputLocation())
                            .build())
                    .build();
            
            StartQueryExecutionResponse response = athenaClient.startQueryExecution(request);
            return response.queryExecutionId();
        } catch (AthenaException e) {
            log.error("Error executing Athena query", e);
            throw e;
        }
    }
    
    public List<Map<String, String>> fetchQueryResults(String queryExecutionId) throws InterruptedException {
        waitForQueryToComplete(queryExecutionId);
        
        GetQueryResultsRequest resultRequest = GetQueryResultsRequest.builder()
                .queryExecutionId(queryExecutionId)
                .build();
        
        GetQueryResultsResponse resultResponse = athenaClient.getQueryResults(resultRequest);
        
        return convertResultsToMap(resultResponse);
    }
    
    private void waitForQueryToComplete(String queryExecutionId) throws InterruptedException {
        GetQueryExecutionRequest request = GetQueryExecutionRequest.builder()
                .queryExecutionId(queryExecutionId)
                .build();
        
        while (true) {
            GetQueryExecutionResponse response = athenaClient.getQueryExecution(request);
            QueryExecutionState state = response.queryExecution().status().state();
            
            if (state == QueryExecutionState.SUCCEEDED) {
                break;
            } else if (state == QueryExecutionState.FAILED || state == QueryExecutionState.CANCELLED) {
                throw new RuntimeException("Query failed: " + 
                    response.queryExecution().status().stateChangeReason());
            }
            
            Thread.sleep(config.getWaitTimeSeconds() * 1000L);
        }
    }
    
    private List<Map<String, String>> convertResultsToMap(GetQueryResultsResponse response) {
        List<Row> rows = response.resultSet().rows();
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> columns = rows.get(0).data().stream()
                .map(Datum::varCharValue)
                .collect(Collectors.toList());
        
        List<Map<String, String>> results = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            Map<String, String> row = new LinkedHashMap<>();
            List<Datum> data = rows.get(i).data();
            
            for (int j = 0; j < columns.size(); j++) {
                String value = j < data.size() ? data.get(j).varCharValue() : "";
                row.put(columns.get(j), value);
            }
            results.add(row);
        }
        
        return results;
    }
}
