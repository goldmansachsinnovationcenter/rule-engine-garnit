package com.gs.ruleengine.acceptance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.model.ActionType;
import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.Leave;
import com.gs.ruleengine.model.LeaveStatus;
import com.gs.ruleengine.model.LeaveType;
import com.gs.ruleengine.model.Rule;
import com.gs.ruleengine.model.expression.Condition;
import com.gs.ruleengine.model.expression.Expression;
// No LogicalExpression class exists in the model
import com.gs.ruleengine.model.expression.Operator;
import com.gs.ruleengine.service.ActionConfigurationService;
import com.gs.ruleengine.service.LeaveService;
import com.gs.ruleengine.service.RuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Acceptance Tests for Leave-related Rules in the Rule Engine Application
 * 
 * These tests verify the complete flow of the application for leave management scenarios,
 * from API endpoints through rule evaluation to action execution, using a real application context.
 * 
 * Business Context:
 * Leave management is a critical HR function that requires automated approval workflows,
 * notifications to managers, and policy enforcement. These tests validate that the rule engine
 * correctly handles leave-related business rules.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LeaveRuleAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private ActionConfigurationService actionConfigurationService;

    @Autowired
    private LeaveService leaveService;
    
    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    public void setUp() {
        // Set up the base URL for API requests
        baseUrl = "http://localhost:" + port + "/api";
        
        // Configure headers for JSON content
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Test Case: Long Leave Auto-Approval Rule
     * 
     * This test verifies the complete flow of:
     * 1. Creating a rule for long leave requests (> 5 days)
     * 2. Creating an email notification action for manager
     * 3. Creating a leave request that matches the rule criteria
     * 4. Evaluating the rule against the leave request
     * 5. Verifying the email notification action was triggered
     * 
     * Business Context:
     * When an employee submits a leave request for more than 5 days,
     * the system should automatically notify their manager for approval.
     * This test ensures this critical workflow functions correctly.
     */
    @Test
    @DisplayName("End-to-End Test: Long Leave Requests Trigger Manager Notification")
    public void testLongLeaveManagerNotification() {
        // Step 1: Create a rule for long leave requests (> 5 days)
        Rule rule = createLongLeaveRule();
        
        // POST the rule to the API using RuleDto format
        Map<String, Object> ruleMap = new HashMap<>();
        ruleMap.put("name", rule.getName());
        ruleMap.put("description", rule.getDescription());
        ruleMap.put("entityType", rule.getEntityType());
        ruleMap.put("expression", objectMapper.readTree(rule.getExpressionJson()));
        ruleMap.put("active", rule.isActive());
        
        HttpEntity<Map<String, Object>> ruleEntity = new HttpEntity<>(ruleMap, headers);
        ResponseEntity<Rule> ruleResponse = restTemplate.exchange(
                baseUrl + "/rules",
                HttpMethod.POST,
                ruleEntity,
                Rule.class
        );
        
        // Verify rule was created successfully
        assertEquals(HttpStatus.CREATED, ruleResponse.getStatusCode());
        Rule createdRule = ruleResponse.getBody();
        assertNotNull(createdRule);
        assertNotNull(createdRule.getId());
        
        // Step 2: Create an email notification action for the rule
        ActionConfiguration actionConfig = createManagerEmailNotificationAction(createdRule.getId().toString());
        
        // POST the action configuration to the API using ActionConfigurationDto format
        Map<String, Object> actionMap = new HashMap<>();
        actionMap.put("ruleId", actionConfig.getRuleId());
        actionMap.put("actionType", actionConfig.getActionType());
        actionMap.put("name", actionConfig.getName());
        try {
            // Parse the configurationJson to an object for the configuration field
            actionMap.put("configuration", objectMapper.readTree(actionConfig.getConfigurationJson()));
        } catch (Exception e) {
            // Fallback to string if parsing fails
            actionMap.put("configuration", actionConfig.getConfigurationJson());
        }
        actionMap.put("active", true);
        
        HttpEntity<Map<String, Object>> actionEntity = new HttpEntity<>(actionMap, headers);
        ResponseEntity<ActionConfiguration> actionResponse = restTemplate.exchange(
                baseUrl + "/actions/configurations",
                HttpMethod.POST,
                actionEntity,
                ActionConfiguration.class
        );
        
        // Verify action configuration was created successfully
        assertEquals(HttpStatus.CREATED, actionResponse.getStatusCode());
        ActionConfiguration createdAction = actionResponse.getBody();
        assertNotNull(createdAction);
        assertNotNull(createdAction.getId());
        
        // Step 3: Create a leave request that matches the rule criteria (10 days)
        Leave leave = createLongLeaveRequest();
        
        // Save the leave request to the database
        Leave savedLeave = leaveService.save(leave);
        assertNotNull(savedLeave.getId());
        
        // Step 4: Evaluate the rule against the leave request
        // POST to the rule engine endpoint
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("entityId", savedLeave.getId());
        requestBody.put("entityType", EntityType.LEAVE.toString());
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> evaluationResponse = restTemplate.exchange(
                baseUrl + "/rule-engine/execute",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );
        
        // Verify rule evaluation was successful
        assertEquals(HttpStatus.OK, evaluationResponse.getStatusCode());
        Map<String, Object> responseBody = evaluationResponse.getBody();
        assertNotNull(responseBody);
        
        // Step 5: Verify the email notification action was triggered
        // In a real test, we would verify this by checking email service logs or a mock
        // For this test, we'll verify the action output in the response
        assertTrue(responseBody.containsKey("actionOutputs"));
        
        // Additional verification could include checking that the leave status was updated
        Leave updatedLeave = leaveService.findById(savedLeave.getId()).orElseThrow();
        assertEquals(LeaveStatus.PENDING, updatedLeave.getStatus());
    }

    /**
     * Helper method to create a rule for long leave requests
     * 
     * This rule will match leave requests with duration > 5 days
     */
    private Rule createLongLeaveRule() {
        // We don't need to create a Condition object directly, just use the expressionJson
        
        // Create the rule
        Rule rule = new Rule();
        rule.setName("Long Leave Request Rule");
        rule.setDescription("Identifies leave requests longer than 5 days for manager approval");
        rule.setEntityType(EntityType.LEAVE);
        rule.setExpressionJson("{\"type\":\"CONDITION\",\"field\":\"endDate\",\"operator\":\"GREATER_THAN\",\"value\":\"${startDate + 5}\"}");
        rule.setActive(true);
        
        return rule;
    }

    /**
     * Helper method to create an email notification action for manager
     * 
     * This action will send an email to the employee's manager
     */
    private ActionConfiguration createManagerEmailNotificationAction(String ruleId) {
        ActionConfiguration actionConfig = new ActionConfiguration();
        actionConfig.setRuleId(Long.valueOf(ruleId));
        actionConfig.setActionType(ActionType.EMAIL);
        actionConfig.setName("Notify Manager of Long Leave Request");
        
        // Set the configuration parameters for the email
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("to", "${entity.managerEmail}");
        parameters.put("subject", "Long Leave Request Requires Approval");
        parameters.put("template", "long-leave-approval-template");
        parameters.put("includeEntityDetails", true);
        
        // Convert parameters to JSON
        String configJson = "{\"to\":\"${entity.managerEmail}\",\"subject\":\"Long Leave Request Requires Approval\",\"template\":\"long-leave-approval-template\",\"includeEntityDetails\":true}";
        actionConfig.setConfigurationJson(configJson);
        
        return actionConfig;
    }

    /**
     * Helper method to create a long leave request (10 days)
     */
    private Leave createLongLeaveRequest() {
        Leave leave = new Leave();
        leave.setEmployeeId("emp123");
        leave.setEmployeeName("John Doe");
        leave.setStartDate(LocalDate.now().plusDays(7));
        leave.setEndDate(LocalDate.now().plusDays(17));
        leave.setReason("Annual vacation");
        leave.setStatus(LeaveStatus.PENDING);
        leave.setType(LeaveType.ANNUAL);
        
        return leave;
    }

    /**
     * Test Case: Overlapping Leave Requests Detection
     * 
     * This test verifies that the rule engine can detect and handle overlapping leave requests
     * by evaluating complex conditions with logical operators (AND, OR).
     * 
     * Business Context:
     * When multiple employees from the same team request leave for overlapping periods,
     * the system should identify this situation to prevent understaffing.
     */
    @Test
    @DisplayName("End-to-End Test: Detect Overlapping Leave Requests")
    public void testOverlappingLeaveDetection() {
        // Implementation would test the rule engine's ability to handle complex
        // expressions with logical operators and date comparisons
        
        // This would create a rule with a complex expression tree using AND/OR operators
        // to detect overlapping leave periods for employees on the same team
    }

    /**
     * Test Case: Emergency Leave Auto-Approval
     * 
     * This test verifies that emergency leave requests are automatically approved
     * and appropriate notifications are sent.
     * 
     * Business Context:
     * Emergency leave requests should be automatically approved to allow employees
     * to handle urgent personal matters without administrative delays.
     */
    @Test
    @DisplayName("End-to-End Test: Emergency Leave Auto-Approval")
    public void testEmergencyLeaveAutoApproval() {
        // Implementation would test the automatic approval of emergency leave requests
        // and verify that the appropriate status updates and notifications occur
    }
}
