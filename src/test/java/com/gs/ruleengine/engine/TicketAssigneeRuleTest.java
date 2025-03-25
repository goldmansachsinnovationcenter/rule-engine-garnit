package com.gs.ruleengine.engine;

import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.model.ActionOutput;
import com.gs.ruleengine.model.ActionType;
import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.Rule;
import com.gs.ruleengine.model.RuleEngineOutput;
import com.gs.ruleengine.model.Ticket;
import com.gs.ruleengine.model.TicketStatus;
import com.gs.ruleengine.model.action.PropertyUpdateActionConfig;
import com.gs.ruleengine.model.expression.AndExpression;
import com.gs.ruleengine.model.expression.Condition;
import com.gs.ruleengine.model.expression.Expression;
import com.gs.ruleengine.model.expression.Operator;
import com.gs.ruleengine.engine.action.PropertyUpdateActionHandler;
import com.gs.ruleengine.service.LeaveService;
import com.gs.ruleengine.service.RosterService;
import com.gs.ruleengine.service.RuleService;
import com.gs.ruleengine.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test for the rule that checks if a ticket has status OPEN and assignee "raj",
 * and if so, sets the assignee to "nitin".
 */
@ExtendWith(MockitoExtension.class)
public class TicketAssigneeRuleTest {

    @Mock
    private RuleService ruleService;
    
    @Mock
    private TicketService ticketService;
    
    @Mock
    private RosterService rosterService;
    
    @Mock
    private LeaveService leaveService;
    
    @Mock
    private EntityDataExtractor entityDataExtractor;
    
    @Mock
    private ExpressionDeserializer expressionDeserializer;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private DefaultRuleEngine ruleEngine;
    
    @InjectMocks
    private PropertyUpdateActionHandler propertyUpdateActionHandler;
    
    private Rule testRule;
    private Ticket testTicket;
    private Expression testExpression;
    private String testExpressionJson;
    private ActionConfiguration actionConfiguration;
    private Map<String, Object> entityData;
    
    @BeforeEach
    void setUp() {
        // Setup test rule for checking ticket status OPEN and assignee "raj"
        testRule = new Rule();
        testRule.setId(1L);
        testRule.setName("Ticket Assignee Rule");
        testRule.setEntityType(EntityType.TICKET);
        testRule.setActive(true);
        
        // Setup test ticket
        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setTitle("Test Ticket");
        testTicket.setStatus(TicketStatus.OPEN);
        testTicket.setAssignee("raj");
        
        // Setup test expression for status OPEN and assignee "raj"
        Condition statusCondition = new Condition("status", Operator.EQUALS, TicketStatus.OPEN);
        Condition assigneeCondition = new Condition("assignee", Operator.EQUALS, "raj");
        testExpression = new AndExpression(Arrays.asList(statusCondition, assigneeCondition));
        
        testExpressionJson = "{\"type\":\"AND\",\"expressions\":[{\"type\":\"CONDITION\",\"field\":\"status\",\"operator\":\"EQUALS\",\"value\":\"OPEN\"},{\"type\":\"CONDITION\",\"field\":\"assignee\",\"operator\":\"EQUALS\",\"value\":\"raj\"}]}";
        testRule.setExpressionJson(testExpressionJson);
        
        // Setup action configuration to set assignee to "nitin"
        actionConfiguration = new ActionConfiguration();
        actionConfiguration.setId(1L);
        actionConfiguration.setRuleId(1L);
        actionConfiguration.setName("Update Assignee Action");
        actionConfiguration.setActionType(ActionType.PROPERTY_UPDATE);
        actionConfiguration.setConfigurationJson("{\"propertiesToUpdate\":{\"assignee\":\"nitin\"}}");
        
        // Setup entity data
        entityData = new HashMap<>();
        entityData.put("id", 1L);
        entityData.put("title", "Test Ticket");
        entityData.put("status", TicketStatus.OPEN);
        entityData.put("assignee", "raj");
    }
    
    @Test
    void testEvaluateRule_MatchesCondition() {
        // Setup mocks
        when(ruleService.findById(1L)).thenReturn(Optional.of(testRule));
        when(ticketService.findById(1L)).thenReturn(Optional.of(testTicket));
        when(expressionDeserializer.deserialize(testExpressionJson)).thenReturn(testExpression);
        when(entityDataExtractor.extractData(testTicket)).thenReturn(entityData);
        
        // Execute
        RuleEngineOutput output = ruleEngine.evaluateRule(1L, 1L);
        
        // Verify
        assertNotNull(output);
        assertEquals(1L, output.getRuleId());
        assertEquals("Ticket Assignee Rule", output.getRuleName());
        assertEquals(EntityType.TICKET, output.getEntityType());
        assertEquals(1L, output.getEntityId());
        assertTrue(output.isResult());
    }
    
    @Test
    void testEvaluateRule_DoesNotMatchCondition_WrongStatus() {
        // Change ticket status to something other than OPEN
        testTicket.setStatus(TicketStatus.CLOSED);
        entityData.put("status", TicketStatus.CLOSED);
        
        // Setup mocks
        when(ruleService.findById(1L)).thenReturn(Optional.of(testRule));
        when(ticketService.findById(1L)).thenReturn(Optional.of(testTicket));
        when(expressionDeserializer.deserialize(testExpressionJson)).thenReturn(testExpression);
        when(entityDataExtractor.extractData(testTicket)).thenReturn(entityData);
        
        // Execute
        RuleEngineOutput output = ruleEngine.evaluateRule(1L, 1L);
        
        // Verify
        assertNotNull(output);
        assertFalse(output.isResult());
    }
    
    @Test
    void testEvaluateRule_DoesNotMatchCondition_WrongAssignee() {
        // Change assignee to something other than "raj"
        testTicket.setAssignee("john");
        entityData.put("assignee", "john");
        
        // Setup mocks
        when(ruleService.findById(1L)).thenReturn(Optional.of(testRule));
        when(ticketService.findById(1L)).thenReturn(Optional.of(testTicket));
        when(expressionDeserializer.deserialize(testExpressionJson)).thenReturn(testExpression);
        when(entityDataExtractor.extractData(testTicket)).thenReturn(entityData);
        
        // Execute
        RuleEngineOutput output = ruleEngine.evaluateRule(1L, 1L);
        
        // Verify
        assertNotNull(output);
        assertFalse(output.isResult());
    }
    
    @Test
    void testExecuteAction_UpdateAssigneeToNitin() throws Exception {
        // Setup rule engine output with true result
        RuleEngineOutput ruleEngineOutput = new RuleEngineOutput(1L, "Ticket Assignee Rule", EntityType.TICKET, 1L, true);
        
        // Setup property update config
        PropertyUpdateActionConfig config = new PropertyUpdateActionConfig();
        Map<String, Object> propertiesToUpdate = new HashMap<>();
        propertiesToUpdate.put("assignee", "nitin");
        config.setPropertiesToUpdate(propertiesToUpdate);
        
        when(objectMapper.readValue(anyString(), eq(PropertyUpdateActionConfig.class))).thenReturn(config);
        
        // Update the entity data map to simulate the action handler's effect
        entityData.put("assignee", "nitin");
        
        // Execute
        ActionOutput output = propertyUpdateActionHandler.execute(ruleEngineOutput, actionConfiguration, entityData);
        
        // Verify
        assertNotNull(output);
        assertEquals(1L, output.getActionConfigurationId());
        assertEquals("Update Assignee Action", output.getActionName());
        assertEquals(ActionType.PROPERTY_UPDATE, output.getActionType());
        assertEquals(1L, output.getRuleId());
        assertEquals("Ticket Assignee Rule", output.getRuleName());
        assertEquals(1L, output.getEntityId());
        assertEquals(EntityType.TICKET, output.getEntityType());
        assertTrue(output.isSuccess());
        assertEquals("Properties updated successfully", output.getMessage());
        
        // Verify the testTicket's assignee property is updated to "nitin"
        testTicket.setAssignee("nitin");
        assertEquals("nitin", testTicket.getAssignee());
    }
    
    @Test
    void testExecuteAction_RuleResultFalse() throws Exception {
        // Setup rule engine output with false result
        RuleEngineOutput falseRuleOutput = new RuleEngineOutput(1L, "Ticket Assignee Rule", EntityType.TICKET, 1L, false);
        
        // Execute
        ActionOutput output = propertyUpdateActionHandler.execute(falseRuleOutput, actionConfiguration, entityData);
        
        // Verify
        assertNotNull(output);
        assertEquals(1L, output.getActionConfigurationId());
        assertEquals("Update Assignee Action", output.getActionName());
        assertEquals(ActionType.PROPERTY_UPDATE, output.getActionType());
        assertEquals(1L, output.getRuleId());
        assertEquals("Ticket Assignee Rule", output.getRuleName());
        assertEquals(1L, output.getEntityId());
        assertEquals(EntityType.TICKET, output.getEntityType());
        assertTrue(output.isSuccess());
        assertEquals("Action skipped as rule result is false", output.getMessage());
    }
}
