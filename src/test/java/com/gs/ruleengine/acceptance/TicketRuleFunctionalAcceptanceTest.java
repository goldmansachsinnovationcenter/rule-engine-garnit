package com.gs.ruleengine.acceptance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.model.ActionType;
import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.Rule;
import com.gs.ruleengine.model.Ticket;
import com.gs.ruleengine.model.TicketStatus;
import com.gs.ruleengine.service.ActionConfigurationService;
import com.gs.ruleengine.service.RuleService;
import com.gs.ruleengine.service.TicketService;
import org.springframework.test.annotation.DirtiesContext;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Acceptance Tests for Ticket-related Rules in the Rule Engine Application
 * 
 * These tests verify the complete flow of the application for ticket management scenarios,
 * from API endpoints through rule evaluation to action execution, using a real application context.
 * 
 * Business Context:
 * Ticket management involves handling support tickets with different statuses, priorities, and assignees.
 * These tests validate that the rule engine correctly applies business rules to tickets.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class TicketRuleFunctionalAcceptanceTest {

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

    private String baseUrl;
    private HttpHeaders headers;
    private List<Ticket> testTickets;

    @BeforeEach
    public void setUp() {
        // Set up the base URL for API requests
        baseUrl = "http://localhost:" + port + "/api";
        
        // Configure headers for JSON content
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create test tickets
        createTestTickets();
    }

    /**
     * Test Case: Batch Ticket Processing with Multiple Rules
     * 
     * This test verifies the complete flow of:
     * 1. Creating a batch of 10 tickets with different statuses, assignees, and priorities
     * 2. Creating rules for ticket processing:
     *    a. If ticket is in open status and priority <= 5, mark it closed and set priority to 0
     *    b. If ticket is in open status and priority >= 7, assign it to Raj and set priority to 4
     *    c. If ticket is in-progress and assignee is SERVICE_QUEUE, assign to Nitin, update priority to 7, and mark as open
     * 3. Creating action configurations for each rule
     * 4. Evaluating the rules against all tickets
     * 5. Verifying the expected state after rule execution:
     *    - Raj has 1 open status ticket with priority 4
     *    - There are 4 tickets in closed status
     *    - Nitin has 5 open status tickets with priority 7
     * 
     * Business Context:
     * This test simulates a real-world scenario where a batch of tickets needs to be processed
     * according to business rules for prioritization and assignment.
     */
    @Test
    @DisplayName("End-to-End Test: Batch Ticket Processing with Multiple Rules")
    public void testBatchTicketProcessingWithMultipleRules() {
        // Step 1: Create rules for ticket processing
        List<Rule> rules = createTicketProcessingRules();
        
        // Step 2: Create action configurations for each rule
        createActionConfigurations(rules);
        
        // Step 3: Process each ticket through the rule engine (first execution)
        processTicketsThroughRuleEngine();
        
        // Step 4: Verify the expected state after first rule execution
        verifyExpectedStateAfterRuleExecution();
        
        // Print ticket states after first execution for debugging
        System.out.println("Ticket states after first rule execution:");
        for (Ticket ticket : ticketService.findAll()) {
            System.out.println("Ticket ID: " + ticket.getId() + 
                    ", Title: " + ticket.getTitle() + 
                    ", Status: " + ticket.getStatus() + 
                    ", Assignee: " + ticket.getAssignee() + 
                    ", Priority: " + ticket.getPriority());
        }
        
        // Step 5: Process tickets through the rule engine again (second execution)
        System.out.println("Executing rules for the second time...");
        processTicketsThroughRuleEngine();
        
        // Step 6: Verify the expected state after second rule execution
        verifyExpectedStateAfterSecondRuleExecution();
        
        // Print final ticket states after second execution for debugging
        System.out.println("Final ticket states after second rule execution:");
        for (Ticket ticket : ticketService.findAll()) {
            System.out.println("Ticket ID: " + ticket.getId() + 
                    ", Title: " + ticket.getTitle() + 
                    ", Status: " + ticket.getStatus() + 
                    ", Assignee: " + ticket.getAssignee() + 
                    ", Priority: " + ticket.getPriority());
        }
    }

    /**
     * Helper method to create test tickets with different statuses, assignees, and priorities
     */
    private void createTestTickets() {
        testTickets = new ArrayList<>();
        
        // 3 open tickets (2 assigned to Raj with priority 5, 1 to Nitin with priority 8)
        Ticket openTicket1 = new Ticket();
        openTicket1.setTitle("Open Ticket 1 for Raj");
        openTicket1.setDescription("This is an open ticket assigned to Raj with priority 5");
        openTicket1.setStatus(TicketStatus.OPEN);
        openTicket1.setAssignee("Raj");
        openTicket1.setPriority(5);
        testTickets.add(openTicket1);
        
        Ticket openTicket2 = new Ticket();
        openTicket2.setTitle("Open Ticket 2 for Raj");
        openTicket2.setDescription("This is another open ticket assigned to Raj with priority 5");
        openTicket2.setStatus(TicketStatus.OPEN);
        openTicket2.setAssignee("Raj");
        openTicket2.setPriority(5);
        testTickets.add(openTicket2);
        
        Ticket openTicket3 = new Ticket();
        openTicket3.setTitle("Open Ticket for Nitin");
        openTicket3.setDescription("This is an open ticket assigned to Nitin with priority 8");
        openTicket3.setStatus(TicketStatus.OPEN);
        openTicket3.setAssignee("Nitin");
        openTicket3.setPriority(8);
        testTickets.add(openTicket3);
        
        // 5 in-progress tickets with assignee SERVICE_QUEUE and priority 10
        for (int i = 1; i <= 5; i++) {
            Ticket inProgressTicket = new Ticket();
            inProgressTicket.setTitle("In Progress Ticket " + i);
            inProgressTicket.setDescription("This is an in-progress ticket assigned to SERVICE_QUEUE with priority 10");
            inProgressTicket.setStatus(TicketStatus.IN_PROGRESS);
            inProgressTicket.setAssignee("SERVICE_QUEUE");
            inProgressTicket.setPriority(10);
            testTickets.add(inProgressTicket);
        }
        
        // 2 closed tickets
        Ticket closedTicket1 = new Ticket();
        closedTicket1.setTitle("Closed Ticket 1");
        closedTicket1.setDescription("This is a closed ticket");
        closedTicket1.setStatus(TicketStatus.CLOSED);
        closedTicket1.setAssignee("Support");
        closedTicket1.setPriority(3);
        testTickets.add(closedTicket1);
        
        Ticket closedTicket2 = new Ticket();
        closedTicket2.setTitle("Closed Ticket 2");
        closedTicket2.setDescription("This is another closed ticket");
        closedTicket2.setStatus(TicketStatus.CLOSED);
        closedTicket2.setAssignee("Support");
        closedTicket2.setPriority(3);
        testTickets.add(closedTicket2);
        
        // Save all tickets to the database
        for (Ticket ticket : testTickets) {
            ticketService.save(ticket);
        }
    }

    /**
     * Helper method to create rules for ticket processing
     */
    private List<Rule> createTicketProcessingRules() {
        List<Rule> rules = new ArrayList<>();
        
        // Rule 1: If ticket is in open status and priority <= 5, mark it closed and set priority to 0
        Rule rule1 = new Rule();
        rule1.setName("Low Priority Open Ticket Rule");
        rule1.setDescription("Closes low priority open tickets");
        rule1.setEntityType(EntityType.TICKET);
        rule1.setExpressionJson("{\"type\":\"AND\",\"expressions\":[{\"type\":\"CONDITION\",\"field\":\"status\",\"operator\":\"EQUALS\",\"value\":\"OPEN\"},{\"type\":\"CONDITION\",\"field\":\"priority\",\"operator\":\"LESS_THAN_OR_EQUALS\",\"value\":\"5\"}]}");
        rule1.setActive(true);
        rules.add(ruleService.save(rule1));
        
        // Rule 2: If ticket is in open status and priority >= 7, assign it to Raj and set priority to 4
        Rule rule2 = new Rule();
        rule2.setName("High Priority Open Ticket Rule");
        rule2.setDescription("Assigns high priority open tickets to Raj");
        rule2.setEntityType(EntityType.TICKET);
        rule2.setExpressionJson("{\"type\":\"AND\",\"expressions\":[{\"type\":\"CONDITION\",\"field\":\"status\",\"operator\":\"EQUALS\",\"value\":\"OPEN\"},{\"type\":\"CONDITION\",\"field\":\"priority\",\"operator\":\"GREATER_THAN_OR_EQUALS\",\"value\":\"7\"}]}");
        rule2.setActive(true);
        rules.add(ruleService.save(rule2));
        
        // Rule 3: If ticket is in-progress and assignee is SERVICE_QUEUE, assign to Nitin, update priority to 7, and mark as open
        Rule rule3 = new Rule();
        rule3.setName("Service Queue Ticket Rule");
        rule3.setDescription("Reassigns service queue tickets to Nitin");
        rule3.setEntityType(EntityType.TICKET);
        rule3.setExpressionJson("{\"type\":\"AND\",\"expressions\":[{\"type\":\"CONDITION\",\"field\":\"status\",\"operator\":\"EQUALS\",\"value\":\"IN_PROGRESS\"},{\"type\":\"CONDITION\",\"field\":\"assignee\",\"operator\":\"EQUALS\",\"value\":\"SERVICE_QUEUE\"}]}");
        rule3.setActive(true);
        rules.add(ruleService.save(rule3));
        
        return rules;
    }

    /**
     * Helper method to create action configurations for each rule
     */
    private void createActionConfigurations(List<Rule> rules) {
        // Action for Rule 1: Close low priority open tickets
        ActionConfiguration action1 = new ActionConfiguration();
        action1.setRuleId(rules.get(0).getId());
        action1.setActionType(ActionType.PROPERTY_UPDATE);
        action1.setName("Close Low Priority Open Tickets");
        action1.setConfigurationJson("{\"propertiesToUpdate\":{\"status\":\"CLOSED\",\"priority\":\"0\"}}");
        action1.setActive(true);
        actionConfigurationService.save(action1);
        
        // Action for Rule 2: Assign high priority open tickets to Raj
        ActionConfiguration action2 = new ActionConfiguration();
        action2.setRuleId(rules.get(1).getId());
        action2.setActionType(ActionType.PROPERTY_UPDATE);
        action2.setName("Assign High Priority Open Tickets to Raj");
        action2.setConfigurationJson("{\"propertiesToUpdate\":{\"assignee\":\"Raj\",\"priority\":\"4\"}}");
        action2.setActive(true);
        actionConfigurationService.save(action2);
        
        // Action for Rule 3: Reassign service queue tickets to Nitin
        ActionConfiguration action3 = new ActionConfiguration();
        action3.setRuleId(rules.get(2).getId());
        action3.setActionType(ActionType.PROPERTY_UPDATE);
        action3.setName("Reassign Service Queue Tickets to Nitin");
        action3.setConfigurationJson("{\"propertiesToUpdate\":{\"assignee\":\"Nitin\",\"priority\":\"7\",\"status\":\"OPEN\"}}");
        action3.setActive(true);
        actionConfigurationService.save(action3);
    }

    /**
     * Helper method to process each ticket through the rule engine
     */
    private void processTicketsThroughRuleEngine() {
        // Get all tickets from the database for processing
        List<Ticket> allTickets = ticketService.findAll();
        
        for (Ticket ticket : allTickets) {
            // Create request body for rule engine execution
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("entityId", ticket.getId());
            requestBody.put("entityType", EntityType.TICKET);
            
            // Execute rule engine for this ticket
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/rule-engine/execute",
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );
            
            // Verify rule execution was successful
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    /**
     * Helper method to verify the expected state after first rule execution
     */
    private void verifyExpectedStateAfterRuleExecution() {
        List<Ticket> allTickets = ticketService.findAll();
        
        // Verify Raj has 1 open status ticket with priority 4
        List<Ticket> rajOpenTickets = allTickets.stream()
                .filter(t -> "Raj".equals(t.getAssignee()) && TicketStatus.OPEN.equals(t.getStatus()) && t.getPriority() == 4)
                .collect(Collectors.toList());
        assertEquals(1, rajOpenTickets.size(), "Raj should have exactly 1 OPEN status ticket with priority 4");
        
        // Verify there are 4 tickets in closed status
        List<Ticket> closedTickets = allTickets.stream()
                .filter(t -> TicketStatus.CLOSED.equals(t.getStatus()))
                .collect(Collectors.toList());
        assertEquals(4, closedTickets.size(), "There should be exactly 4 tickets with CLOSED status");
        
        // Verify Nitin has 5 open status tickets with priority 7
        List<Ticket> nitinOpenTickets = allTickets.stream()
                .filter(t -> "Nitin".equals(t.getAssignee()) && TicketStatus.OPEN.equals(t.getStatus()) && t.getPriority() == 7)
                .collect(Collectors.toList());
        assertEquals(5, nitinOpenTickets.size(), "Nitin should have exactly 5 OPEN status tickets with priority 7");
    }
    
    /**
     * Helper method to verify the expected state after second rule execution
     * 
     * Validation criteria after second execution:
     * 1. There should be exactly 5 tickets with a status of "Closed"
     * 2. Raj should have 5 tickets with an "Open" status and a priority of 4
     * 3. Both Nitin and SERVICE_QUEUE should have no tickets allocated
     */
    private void verifyExpectedStateAfterSecondRuleExecution() {
        List<Ticket> allTickets = ticketService.findAll();
        
        // Verify there are exactly 5 tickets with a status of "Closed"
        List<Ticket> closedTickets = allTickets.stream()
                .filter(t -> TicketStatus.CLOSED.equals(t.getStatus()))
                .collect(Collectors.toList());
        assertEquals(5, closedTickets.size(), "There should be exactly 5 tickets with CLOSED status after second execution");
        
        // Verify Raj has 5 tickets with an "Open" status and a priority of 4
        List<Ticket> rajOpenTickets = allTickets.stream()
                .filter(t -> "Raj".equals(t.getAssignee()) && TicketStatus.OPEN.equals(t.getStatus()) && t.getPriority() == 4)
                .collect(Collectors.toList());
        assertEquals(5, rajOpenTickets.size(), "Raj should have exactly 5 OPEN status tickets with priority 4 after second execution");
        
        // Verify Nitin has no tickets allocated
        List<Ticket> nitinTickets = allTickets.stream()
                .filter(t -> "Nitin".equals(t.getAssignee()))
                .collect(Collectors.toList());
        assertEquals(0, nitinTickets.size(), "Nitin should have no tickets allocated after second execution");
        
        // Verify SERVICE_QUEUE has no tickets allocated
        List<Ticket> serviceQueueTickets = allTickets.stream()
                .filter(t -> "SERVICE_QUEUE".equals(t.getAssignee()))
                .collect(Collectors.toList());
        assertEquals(0, serviceQueueTickets.size(), "SERVICE_QUEUE should have no tickets allocated after second execution");
    }
}
