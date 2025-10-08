package com.cloudaudit.service;

import com.cloudaudit.config.CloudAuditConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SqlQueryService {
    
    private final BedrockService bedrockService;
    private final CloudAuditConfig config;
    
    public SqlQueryService(BedrockService bedrockService, CloudAuditConfig config) {
        this.bedrockService = bedrockService;
        this.config = config;
    }
    
    public String generateSqlQuery(String userQuery, String context) {
        String prompt = buildPrompt(userQuery, context);
        String response = bedrockService.invokeClaude(prompt);
        String cleanedQuery = cleanSqlQuery(response);
        
        if (!cleanedQuery.toUpperCase().trim().startsWith("SELECT")) {
            throw new IllegalArgumentException("Generated query does not start with SELECT");
        }
        
        return cleanedQuery;
    }
    
    public String describeResults(String userQuery, List<Map<String, String>> results, boolean isPartial) {
        StringBuilder resultsText = new StringBuilder();
        
        if (!results.isEmpty()) {
            resultsText.append(String.join("\t", results.get(0).keySet())).append("\n");
            
            int limit = Math.min(100, results.size());
            for (int i = 0; i < limit; i++) {
                resultsText.append(String.join("\t", results.get(i).values())).append("\n");
            }
        }
        
        String prompt = String.format(
            "Given the following user query and results, provide a detailed description of the data:\n" +
            "User Query: %s\n" +
            "Results:\n%s\n\n" +
            "Respond in plain language with clear, actionable insights.%s",
            userQuery,
            resultsText.toString(),
            isPartial ? " Note: This description is for the first 100 rows only." : ""
        );
        
        return bedrockService.invokeClaude(prompt);
    }
    
    private String buildPrompt(String userQuery, String context) {
        return String.format(
            "Generate a SQL query that answers the user's question. The query should start with SELECT and end with a semicolon.\n" +
            "Do not include any explanatory text - provide ONLY the SQL query.\n\n" +
            "Context: %s\n" +
            "User Query: %s\n\n" +
            "Remember: Return ONLY the SQL query, starting with SELECT and ending with a semicolon.",
            context,
            userQuery
        );
    }
    
    private String cleanSqlQuery(String query) {
        Pattern selectPattern = Pattern.compile("SELECT\\s+.*?(?:;|$)", 
                                               Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = selectPattern.matcher(query);
        
        if (matcher.find()) {
            query = matcher.group(0);
        }
        
        query = query.replaceAll("\\s+", " ").trim();
        
        if (!query.endsWith(";")) {
            query += ";";
        }
        
        return query;
    }
}
