package com.cloudaudit.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    @DisplayName("QueryRequest should set and get userQuery")
    void testQueryRequest() {
        QueryRequest request = new QueryRequest();
        request.setUserQuery("Show me events");
        
        assertEquals("Show me events", request.getUserQuery());
    }

    @Test
    @DisplayName("QueryRequest constructor should work")
    void testQueryRequestConstructor() {
        QueryRequest request = new QueryRequest("Test query");
        
        assertEquals("Test query", request.getUserQuery());
    }

    @Test
    @DisplayName("QueryResponse should set and get all fields")
    void testQueryResponse() {
        QueryResponse response = new QueryResponse();
        
        response.setSqlQuery("SELECT *");
        response.setResults("Results here");
        response.setDescription("Description");
        response.setPartial(true);
        response.setCsvData("csv,data");
        response.setError("Error message");
        
        assertEquals("SELECT *", response.getSqlQuery());
        assertEquals("Results here", response.getResults());
        assertEquals("Description", response.getDescription());
        assertTrue(response.isPartial());
        assertEquals("csv,data", response.getCsvData());
        assertEquals("Error message", response.getError());
    }
}
