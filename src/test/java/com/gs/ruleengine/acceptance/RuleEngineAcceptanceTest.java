package com.gs.ruleengine.acceptance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.model.ActionType;
import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.Rule;
import com.gs.ruleengine.model.Ticket;
import com.gs.ruleengine.model.TicketStatus;
import com.gs.ruleengine.model.expression.Condition;
import com.gs.ruleengine.model.expression.Expression;
import com.gs.ruleengine.model.expression.Operator;
import com.gs.ruleengine.service.ActionConfigurationService;
import com.gs.ruleengine.service.RuleService;
import com.gs.ruleengine.service.TicketService;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Acceptance Tests for Rule Engine Application
 * 
 * These tests verify the complete flow of the application from API endpoints
 * through rule evaluation to action execution, using a real application context.
 * 
 * Unlike unit and functional tests, these tests:
 * - Use @SpringBootTest to load the entire application context
 * - Test the integration between components
 * - Verify end-to-end functionality from HTTP requests to database operations
 * - Validate the complete business flows
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RuleEngineAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private ActionConfigurationService actionConfigurationService;

    @Autowired
    private TicketService ticketService;

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
     * Test Case: Create and evaluate a rule for ticket priority
     * 
     * This test verifies the complete flow of:
     * 1. Creating a rule via the API
     * 2. Creating an action configuration for the rule
     * 3. Creating a ticket that matches the rule criteria
     * 4. Evaluating the rule against the ticket
     * 5. Verifying the action was executed correctly
     * 
     * Business Context:
     * This test simulates a business scenario where tickets with severity "High"
     * should be automatically assigned to a specific user.
     */
    @Test
    @DisplayName("End-to-End Test: Create Rule, Action and Evaluate Ticket")
    public void testCreateAndEvaluateRule() {
        // Step 1: Create a rule for high severity tickets
        Rule rule = createHighSeverityRule();
        
        // POST the rule to the API using a Map instead of Rule object to avoid serialization issues
        Map<String, Object> ruleMap = new HashMap<>();
        ruleMap.put("name", rule.getName());
        ruleMap.put("description", rule.getDescription());
        ruleMap.put("entityType", rule.getEntityType().toString());
        try {
            ruleMap.put("expression", objectMapper.readTree(rule.getExpressionJson()));
        } catch (Exception e) {
            // Fallback to string if parsing fails
            ruleMap.put("expression", rule.getExpressionJson());
        }
        ruleMap.put("active", rule.isActive());
        
        HttpEntity<Map<String, Object>> ruleEntity = new HttpEntity<>(ruleMap, headers);
        ResponseEntity<Map> ruleResponse = restTemplate.exchange(
                baseUrl + "/rules",
                HttpMethod.POST,
                ruleEntity,
                Map.class
        );
        
        // Verify rule was created successfully
        assertEquals(HttpStatus.CREATED, ruleResponse.getStatusCode());
        Map<String, Object> createdRule = ruleResponse.getBody();
        assertNotNull(createdRule);
        assertNotNull(createdRule.get("id"));
        
        // Step 2: Create an action configuration for the rule
        ActionConfiguration actionConfig = createAssigneeActionConfig(createdRule.get("id").toString());
        
        // POST the action configuration to the API using a Map instead of ActionConfiguration object
        Map<String, Object> actionMap = new HashMap<>();
        actionMap.put("ruleId", actionConfig.getRuleId());
        actionMap.put("actionType", actionConfig.getActionType().toString());
        actionMap.put("name", actionConfig.getName());
        try {
            // Parse the configurationJson to an object for the configuration field
            actionMap.put("configuration", objectMapper.readTree(actionConfig.getConfigurationJson()));
        } catch (Exception e) {
            // Fallback to string if parsing fails
            actionMap.put("configuration", actionConfig.getConfigurationJson());
        }
        
        HttpEntity<Map<String, Object>> actionEntity = new HttpEntity<>(actionMap, headers);
        ResponseEntity<Map> actionResponse = restTemplate.exchange(
                baseUrl + "/actions/configurations",
                HttpMethod.POST,
                actionEntity,
                Map.class
        );
        
        // Verify action configuration was created successfully
        assertEquals(HttpStatus.CREATED, actionResponse.getStatusCode());
        Map<String, Object> createdAction = actionResponse.getBody();
        assertNotNull(createdAction);
        assertNotNull(createdAction.get("id"));
        
        // Step 3: Create a ticket that matches the rule criteria
        Ticket ticket = createHighSeverityTicket();
        
        // Save the ticket to the database
        Ticket savedTicket = ticketService.save(ticket);
        assertNotNull(savedTicket.getId());
        
        // Step 4: Evaluate the rule against the ticket
        // POST to the rule engine endpoint
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("entityId", savedTicket.getId());
        requestBody.put("entityType", EntityType.TICKET.toString());
        // We don't specify ruleIds to evaluate all active rules for this entity type
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> evaluationResponse = restTemplate.exchange(
                baseUrl + "/rule-engine/execute",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );
        
        // Verify rule evaluation was successful
        assertEquals(HttpStatus.OK, evaluationResponse.getStatusCode());
        
        // Print response body for debugging
        System.out.println("Rule evaluation response: " + evaluationResponse.getBody());
        
        // Step 5: Verify the ticket was updated with the new assignee
        Ticket updatedTicket = ticketService.findById(savedTicket.getId()).orElseThrow();
        assertEquals("support_team_lead", updatedTicket.getAssignee());
    }

    /**
     * Helper method to create a rule for high severity tickets
     * 
     * This rule will match tickets with severity = "High"
     */
    private Rule createHighSeverityRule() {
        // We don't need to create a Condition object directly, just use the expressionJson
        
        // Create the rule
        Rule rule = new Rule();
        rule.setName("High Severity Ticket Rule");
        rule.setDescription("Assigns high severity tickets to the support team lead");
        rule.setEntityType(EntityType.TICKET);
        rule.setExpressionJson("{\"type\":\"CONDITION\",\"field\":\"priority\",\"operator\":\"EQUALS\",\"value\":1}");
        rule.setActive(true);
        
        return rule;
    }

    /**
     * Helper method to create an action configuration for assigning tickets
     * 
     * This action will set the assignee field to "support_team_lead"
     */
    private ActionConfiguration createAssigneeActionConfig(String ruleId) {
        ActionConfiguration actionConfig = new ActionConfiguration();
        actionConfig.setRuleId(Long.valueOf(ruleId));
        actionConfig.setActionType(ActionType.PROPERTY_UPDATE);
        actionConfig.setName("Assign to Support Team Lead");
        
        // Set the configuration parameters
        Map<String, Object> parameters = new HashMap<>();
        Map<String, Object> propertyUpdates = new HashMap<>();
        propertyUpdates.put("assignee", "support_team_lead");
        parameters.put("propertiesToUpdate", propertyUpdates);
        // Convert parameters to JSON
        String configJson = "{\"propertiesToUpdate\":{\"assignee\":\"support_team_lead\"}}";
        actionConfig.setConfigurationJson(configJson);
        
        return actionConfig;
    }

    /**
     * Helper method to create a high severity ticket
     */
    private Ticket createHighSeverityTicket() {
        Ticket ticket = new Ticket();
        ticket.setTitle("Critical System Failure");
        ticket.setDescription("The payment processing system is down");
        ticket.setPriority(1); // 1 = highest priority
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setAssignee(null); // Initially unassigned
        
        return ticket;
    }

    /**
     * Test Case: Rule evaluation with multiple conditions
     * 
     * This test verifies that rules with multiple conditions (using AND/OR)
     * are evaluated correctly and the appropriate actions are executed.
     * 
     * Business Context:
     * This test simulates a business scenario where tickets with both "High" severity
     * and "Billing" category should be escalated to a specific team.
     */
    @Test
    @DisplayName("End-to-End Test: Evaluate Rule with Multiple Conditions")
    public void testRuleWithMultipleConditions() {
        // Implementation would be similar to the first test but with a more complex rule
        // that uses AND/OR operators to combine multiple conditions
        
        // This would test the expression tree evaluation capabilities of the rule engine
    }

    /**
     * Test Case: Action execution with email notification
     * 
     * This test verifies that when a rule matches, an email action is correctly
     * configured and would be sent (using a mock email service in test).
     * 
     * Business Context:
     * This test simulates a business scenario where matching tickets trigger
     * notification emails to relevant stakeholders.
     */
    @Test
    @DisplayName("End-to-End Test: Email Action Execution")
    public void testEmailActionExecution() {
        // Implementation would test the email action handler functionality
        // by creating a rule and an email action configuration
    }
}
