package com.cloudaudit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {
    private String sqlQuery;
    private String results;
    private String description;
    private boolean isPartial;
    private String csvData;
    private String error;
}
