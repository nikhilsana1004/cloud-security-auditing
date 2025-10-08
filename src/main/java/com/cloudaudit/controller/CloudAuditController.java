package com.cloudaudit.controller;

import com.cloudaudit.config.CloudAuditConfig;
import com.cloudaudit.dto.QueryRequest;
import com.cloudaudit.dto.QueryResponse;
import com.cloudaudit.service.AthenaService;
import com.cloudaudit.service.SqlQueryService;
import com.cloudaudit.service.DataMaskingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class CloudAuditController {
    
    private final AthenaService athenaService;
    private final SqlQueryService sqlQueryService;
    private final DataMaskingService dataMaskingService;
    private final CloudAuditConfig config;
    
    public CloudAuditController(AthenaService athenaService, 
                               SqlQueryService sqlQueryService,
                               DataMaskingService dataMaskingService,
                               CloudAuditConfig config) {
        this.athenaService = athenaService;
        this.sqlQueryService = sqlQueryService;
        this.dataMaskingService = dataMaskingService;
        this.config = config;
    }
    
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Cloud Security Auditing");
        return "index";
    }
    
    @PostMapping("/api/query")
    @ResponseBody
    public ResponseEntity<QueryResponse> executeQuery(@RequestBody QueryRequest request) {
        try {
            // Log query without sensitive data
            log.info("Processing query request");
            
            String initialQuery = String.format("SELECT * FROM %s LIMIT 100", config.getTableName());
            String queryExecutionId = athenaService.executeQuery(initialQuery);
            List<Map<String, String>> sampleData = athenaService.fetchQueryResults(queryExecutionId);
            
            if (sampleData.isEmpty()) {
                QueryResponse response = new QueryResponse();
                response.setError("No data found in the sample query");
                return ResponseEntity.ok(response);
            }
            
            String context = buildContext(sampleData);
            String sqlQuery = sqlQueryService.generateSqlQuery(request.getUserQuery(), context);
            
            String queryExecId = athenaService.executeQuery(sqlQuery);
            List<Map<String, String>> results = athenaService.fetchQueryResults(queryExecId);
            
            // SECURITY: Mask all sensitive data before returning
            List<Map<String, String>> maskedResults = dataMaskingService.maskSensitiveData(results);
            
            QueryResponse response = new QueryResponse();
            // SECURITY: Mask SQL query to hide database structure
            response.setSqlQuery(dataMaskingService.maskSqlQuery(sqlQuery));
            
            if (maskedResults.isEmpty()) {
                response.setResults("No data found matching your query.");
                response.setDescription("");
            } else {
                boolean isPartial = maskedResults.size() > 100;
                response.setPartial(isPartial);
                response.setResults(formatResults(maskedResults, isPartial ? 100 : maskedResults.size()));
                
                String description = sqlQueryService.describeResults(
                    request.getUserQuery(), 
                    maskedResults, 
                    isPartial
                );
                response.setDescription(description);
                
                if (isPartial) {
                    response.setCsvData(convertToCsv(maskedResults));
                }
            }
            
            log.info("Query processed successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing query: {}", e.getMessage());
            QueryResponse response = new QueryResponse();
            response.setError("Error processing query. Please try again.");
            return ResponseEntity.ok(response);
        }
    }
    
    private String buildContext(List<Map<String, String>> sampleData) {
        StringBuilder context = new StringBuilder();
        context.append("Database: [REDACTED]")
               .append(", Table: [REDACTED]")
               .append("\n\nColumns:\n");
        
        if (!sampleData.isEmpty()) {
            context.append(String.join("\n", sampleData.get(0).keySet()));
            // SECURITY: Do not include sample data to avoid exposing sensitive info
        }
        
        return context.toString();
    }
    
    private String formatResults(List<Map<String, String>> results, int limit) {
        StringBuilder sb = new StringBuilder();
        
        if (!results.isEmpty()) {
            sb.append(String.join(" | ", results.get(0).keySet())).append("\n");
            sb.append("-".repeat(80)).append("\n");
            
            for (int i = 0; i < limit && i < results.size(); i++) {
                sb.append(String.join(" | ", results.get(i).values())).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    private String convertToCsv(List<Map<String, String>> results) {
        try {
            StringWriter sw = new StringWriter();
            
            if (!results.isEmpty()) {
                String[] headers = results.get(0).keySet().toArray(new String[0]);
                
                CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader(headers));
                
                for (Map<String, String> row : results) {
                    printer.printRecord(row.values());
                }
                
                printer.flush();
            }
            
            return sw.toString();
        } catch (Exception e) {
            log.error("Error converting to CSV", e);
            return "";
        }
    }
}
