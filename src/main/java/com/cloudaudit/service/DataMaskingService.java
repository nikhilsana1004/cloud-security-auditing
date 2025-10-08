package com.cloudaudit.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class DataMaskingService {
    
    private static final Set<String> SENSITIVE_COLUMNS = Set.of(
        "useridentity", "sourceipaddress", "requestparameters", 
        "responseelements", "resources", "recipientaccountid",
        "vpcendpointid", "sessioncredentialfromconsole",
        "additionaleventdata", "sharedeventid"
    );
    
    private static final Pattern IP_PATTERN = Pattern.compile(
        "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"
    );
    
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile(
        "\\b\\d{12}\\b"
    );
    
    private static final Pattern ARN_PATTERN = Pattern.compile(
        "arn:aws:[a-z0-9-]+:[a-z0-9-]*:\\d{12}:[a-zA-Z0-9-/]+"
    );
    
    private static final Pattern ACCESS_KEY_PATTERN = Pattern.compile(
        "AKIA[0-9A-Z]{16}"
    );
    
    public List<Map<String, String>> maskSensitiveData(List<Map<String, String>> data) {
        List<Map<String, String>> maskedData = new ArrayList<>();
        
        for (Map<String, String> row : data) {
            Map<String, String> maskedRow = new LinkedHashMap<>();
            
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String key = entry.getKey().toLowerCase();
                String value = entry.getValue();
                
                if (value == null || value.isEmpty()) {
                    maskedRow.put(entry.getKey(), value);
                    continue;
                }
                
                if (SENSITIVE_COLUMNS.contains(key)) {
                    maskedRow.put(entry.getKey(), maskValue(value));
                } else {
                    maskedRow.put(entry.getKey(), maskPatternsInValue(value));
                }
            }
            
            maskedData.add(maskedRow);
        }
        
        return maskedData;
    }
    
    private String maskValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        // For very short values
        if (value.length() <= 4) {
            return "****";
        }
        
        // Show first 2 and last 2 characters
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
    
    private String maskPatternsInValue(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        // Mask AWS Access Keys
        value = ACCESS_KEY_PATTERN.matcher(value).replaceAll("AKIA****************");
        
        // Mask IP addresses
        value = IP_PATTERN.matcher(value).replaceAll("***.***.***.***");
        
        // Mask AWS Account IDs
        value = ACCOUNT_PATTERN.matcher(value).replaceAll("************");
        
        // Mask ARNs
        value = ARN_PATTERN.matcher(value).replaceAll("arn:aws:***:***:************:***");
        
        return value;
    }
    
    public String maskSqlQuery(String query) {
        if (query == null || query.isEmpty()) {
            return query;
        }
        
        // Remove actual database and table names from displayed query
        return query
            .replaceAll("FROM\\s+[a-zA-Z0-9_]+\\.[a-zA-Z0-9_]+", "FROM [DATABASE].[TABLE]")
            .replaceAll("FROM\\s+[a-zA-Z0-9_]+", "FROM [TABLE]")
            .replaceAll("database\\s*=\\s*'[^']+'", "database='[REDACTED]'");
    }
}
