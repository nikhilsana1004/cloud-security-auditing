package com.cloudaudit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DataMaskingServiceTest {

    private DataMaskingService dataMaskingService;

    @BeforeEach
    void setUp() {
        dataMaskingService = new DataMaskingService();
    }

    @Test
    @DisplayName("Should mask sensitive column values")
    void testMaskSensitiveColumns() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> row = new HashMap<>();
        row.put("eventname", "ConsoleLogin");
        row.put("sourceipaddress", "192.168.1.100");
        data.add(row);

        List<Map<String, String>> result = dataMaskingService.maskSensitiveData(data);

        assertEquals("19****00", result.get(0).get("sourceipaddress"));
        assertEquals("ConsoleLogin", result.get(0).get("eventname"));
    }

    @Test
    @DisplayName("Should mask IP addresses in non-sensitive columns")
    void testMaskIpAddressesInNonSensitiveColumns() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> row = new HashMap<>();
        row.put("eventname", "Login from 192.168.1.100");
        row.put("errormessage", "Failed login from 10.0.0.1");
        data.add(row);

        List<Map<String, String>> result = dataMaskingService.maskSensitiveData(data);

        assertTrue(result.get(0).get("eventname").contains("***.***.***.***"));
        assertTrue(result.get(0).get("errormessage").contains("***.***.***.***"));
    }

    @Test
    @DisplayName("Should mask account IDs in values")
    void testMaskAccountIds() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> row = new HashMap<>();
        row.put("eventname", "AssumeRole");
        row.put("errormessage", "Account 123456789012 denied");
        data.add(row);

        List<Map<String, String>> result = dataMaskingService.maskSensitiveData(data);

        assertTrue(result.get(0).get("errormessage").contains("************"));
        assertFalse(result.get(0).get("errormessage").contains("123456789012"));
    }

    @Test
    @DisplayName("Should mask SQL queries")
    void testMaskSqlQuery() {
        String query = "SELECT * FROM my_database.my_table WHERE id = 1";
        String result = dataMaskingService.maskSqlQuery(query);

        assertTrue(result.contains("[TABLE]") || result.contains("[DATABASE]"));
        assertFalse(result.contains("my_database"));
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testHandleNullValues() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> row = new HashMap<>();
        row.put("eventname", "Login");
        row.put("sourceipaddress", null);
        data.add(row);

        List<Map<String, String>> result = dataMaskingService.maskSensitiveData(data);

        assertNull(result.get(0).get("sourceipaddress"));
    }

    @Test
    @DisplayName("Should handle empty data")
    void testHandleEmptyData() {
        List<Map<String, String>> data = new ArrayList<>();
        List<Map<String, String>> result = dataMaskingService.maskSensitiveData(data);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should mask short values in sensitive columns")
    void testMaskShortValues() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> row = new HashMap<>();
        row.put("useridentity", "abc");
        data.add(row);

        List<Map<String, String>> result = dataMaskingService.maskSensitiveData(data);

        assertEquals("****", result.get(0).get("useridentity"));
    }

    @Test
    @DisplayName("Should mask account IDs within ARNs")
    void testMaskArns() {
        List<Map<String, String>> data = new ArrayList<>();
        Map<String, String> row = new HashMap<>();
        row.put("eventname", "GetUser with arn:aws:iam::123456789012:user/john");
        data.add(row);

        List<Map<String, String>> result = dataMaskingService.maskSensitiveData(data);

        // The account ID (123456789012) within the ARN should be masked
        String maskedValue = result.get(0).get("eventname");
        assertTrue(maskedValue.contains("************"), "Expected masked account ID but got: " + maskedValue);
        assertFalse(maskedValue.contains("123456789012"), "Account ID should be masked");
        // ARN structure should still be visible
        assertTrue(maskedValue.contains("arn:aws:iam::"), "ARN structure should be preserved");
    }
}
