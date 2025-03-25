package com.gs.ruleengine.engine;

import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.Rule;
import com.gs.ruleengine.model.RuleEngineOutput;
import com.gs.ruleengine.model.Ticket;
import com.gs.ruleengine.model.TicketStatus;
import com.gs.ruleengine.model.expression.AndExpression;
import com.gs.ruleengine.model.expression.Condition;
import com.gs.ruleengine.model.expression.Expression;
import com.gs.ruleengine.model.expression.Operator;
import com.gs.ruleengine.model.expression.OrExpression;
import com.gs.ruleengine.service.LeaveService;
import com.gs.ruleengine.service.RosterService;
import com.gs.ruleengine.service.RuleService;
import com.gs.ruleengine.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEngineTest {

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
    
    @InjectMocks
    private DefaultRuleEngine ruleEngine;
    
    private Rule testRule;
    private Ticket testTicket;
    private Expression testExpression;
    private String testExpressionJson;
    
    @BeforeEach
    void setUp() {
        // Setup test rule
        testRule = new Rule();
        testRule.setId(1L);
        testRule.setName("Test Rule");
        testRule.setEntityType(EntityType.TICKET);
        testRule.setActive(true);
        
        // Setup test ticket
        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setTitle("Test Ticket");
        testTicket.setStatus(TicketStatus.OPEN);
        testTicket.setPriority(1);
        
        // Setup test expression
        Condition condition1 = new Condition("status", Operator.EQUALS, TicketStatus.OPEN);
        Condition condition2 = new Condition("priority", Operator.EQUALS, 1);
        testExpression = new AndExpression(Arrays.asList(condition1, condition2));
        
        testExpressionJson = "{\"type\":\"AND\",\"expressions\":[{\"type\":\"CONDITION\",\"field\":\"status\",\"operator\":\"EQUALS\",\"value\":\"OPEN\"},{\"type\":\"CONDITION\",\"field\":\"priority\",\"operator\":\"EQUALS\",\"value\":1}]}";
        testRule.setExpressionJson(testExpressionJson);
    }
    
    @Test
    void testEvaluateRule_Success() {
        // Setup mocks
        when(ruleService.findById(1L)).thenReturn(Optional.of(testRule));
        when(ticketService.findById(1L)).thenReturn(Optional.of(testTicket));
        when(expressionDeserializer.deserialize(testExpressionJson)).thenReturn(testExpression);
        
        Map<String, Object> ticketData = Map.of(
            "id", 1L,
            "title", "Test Ticket",
            "status", TicketStatus.OPEN,
            "priority", 1
        );
        when(entityDataExtractor.extractData(testTicket)).thenReturn(ticketData);
        
        // Execute
        RuleEngineOutput output = ruleEngine.evaluateRule(1L, 1L);
        
        // Verify
        assertNotNull(output);
        assertEquals(1L, output.getRuleId());
        assertEquals("Test Rule", output.getRuleName());
        assertEquals(EntityType.TICKET, output.getEntityType());
        assertEquals(1L, output.getEntityId());
        assertTrue(output.isResult());
    }
    
    @Test
    void testEvaluateRule_RuleNotFound() {
        // Setup mocks
        when(ruleService.findById(anyLong())).thenReturn(Optional.empty());
        
        // Execute
        RuleEngineOutput output = ruleEngine.evaluateRule(1L, 1L);
        
        // Verify
        assertNull(output);
    }
    
    @Test
    void testEvaluateRule_EntityNotFound() {
        // Setup mocks
        when(ruleService.findById(1L)).thenReturn(Optional.of(testRule));
        when(ticketService.findById(anyLong())).thenReturn(Optional.empty());
        
        // Execute
        RuleEngineOutput output = ruleEngine.evaluateRule(1L, 1L);
        
        // Verify
        assertNull(output);
    }
    
    @Test
    void testEvaluateRule_ExpressionDeserializationFailed() {
        // Setup mocks
        when(ruleService.findById(1L)).thenReturn(Optional.of(testRule));
        when(ticketService.findById(1L)).thenReturn(Optional.of(testTicket));
        when(expressionDeserializer.deserialize(any())).thenReturn(null);
        
        Map<String, Object> ticketData = Map.of(
            "id", 1L,
            "title", "Test Ticket",
            "status", TicketStatus.OPEN,
            "priority", 1
        );
        when(entityDataExtractor.extractData(testTicket)).thenReturn(ticketData);
        
        // Execute
        RuleEngineOutput output = ruleEngine.evaluateRule(1L, 1L);
        
        // Verify
        assertNull(output);
    }
    
    @Test
    void testEvaluateRules_Success() {
        // Setup mocks
        Rule rule1 = new Rule();
        rule1.setId(1L);
        rule1.setName("Rule 1");
        rule1.setEntityType(EntityType.TICKET);
        rule1.setExpressionJson("{\"type\":\"CONDITION\",\"field\":\"status\",\"operator\":\"EQUALS\",\"value\":\"OPEN\"}");
        rule1.setActive(true);
        
        Rule rule2 = new Rule();
        rule2.setId(2L);
        rule2.setName("Rule 2");
        rule2.setEntityType(EntityType.TICKET);
        rule2.setExpressionJson("{\"type\":\"CONDITION\",\"field\":\"priority\",\"operator\":\"EQUALS\",\"value\":1}");
        rule2.setActive(true);
        
        List<Rule> rules = Arrays.asList(rule1, rule2);
        
        when(ruleService.findActiveRulesByEntityType(EntityType.TICKET)).thenReturn(rules);
        when(ticketService.findById(1L)).thenReturn(Optional.of(testTicket));
        
        Condition condition1 = new Condition("status", Operator.EQUALS, TicketStatus.OPEN);
        when(expressionDeserializer.deserialize(rule1.getExpressionJson())).thenReturn(condition1);
        
        Condition condition2 = new Condition("priority", Operator.EQUALS, 1);
        when(expressionDeserializer.deserialize(rule2.getExpressionJson())).thenReturn(condition2);
        
        Map<String, Object> ticketData = Map.of(
            "id", 1L,
            "title", "Test Ticket",
            "status", TicketStatus.OPEN,
            "priority", 1
        );
        when(entityDataExtractor.extractData(testTicket)).thenReturn(ticketData);
        
        // Execute
        List<RuleEngineOutput> outputs = ruleEngine.evaluateRules(EntityType.TICKET, 1L);
        
        // Verify
        assertNotNull(outputs);
        assertEquals(2, outputs.size());
        
        RuleEngineOutput output1 = outputs.get(0);
        assertEquals(1L, output1.getRuleId());
        assertEquals("Rule 1", output1.getRuleName());
        assertEquals(EntityType.TICKET, output1.getEntityType());
        assertEquals(1L, output1.getEntityId());
        assertTrue(output1.isResult());
        
        RuleEngineOutput output2 = outputs.get(1);
        assertEquals(2L, output2.getRuleId());
        assertEquals("Rule 2", output2.getRuleName());
        assertEquals(EntityType.TICKET, output2.getEntityType());
        assertEquals(1L, output2.getEntityId());
        assertTrue(output2.isResult());
    }
    
    @Test
    void testEvaluateRules_NoRulesFound() {
        // Setup mocks
        when(ruleService.findActiveRulesByEntityType(any())).thenReturn(List.of());
        
        // Execute
        List<RuleEngineOutput> outputs = ruleEngine.evaluateRules(EntityType.TICKET, 1L);
        
        // Verify
        assertNotNull(outputs);
        assertTrue(outputs.isEmpty());
    }
    
    @Test
    void testEvaluateRules_EntityNotFound() {
        // Setup mocks
        Rule rule = new Rule();
        rule.setId(1L);
        rule.setName("Rule 1");
        rule.setEntityType(EntityType.TICKET);
        rule.setExpressionJson("{\"type\":\"CONDITION\",\"field\":\"status\",\"operator\":\"EQUALS\",\"value\":\"OPEN\"}");
        rule.setActive(true);
        
        when(ruleService.findActiveRulesByEntityType(EntityType.TICKET)).thenReturn(List.of(rule));
        when(ticketService.findById(anyLong())).thenReturn(Optional.empty());
        
        // Execute
        List<RuleEngineOutput> outputs = ruleEngine.evaluateRules(EntityType.TICKET, 1L);
        
        // Verify
        assertNotNull(outputs);
        assertTrue(outputs.isEmpty());
    }
    
    @Test
    void testEvaluateRuleWithData_Success() {
        // Setup mocks
        when(ruleService.findById(1L)).thenReturn(Optional.of(testRule));
        when(expressionDeserializer.deserialize(testExpressionJson)).thenReturn(testExpression);
        
        Map<String, Object> entityData = Map.of(
            "id", 1L,
            "title", "Test Ticket",
            "status", TicketStatus.OPEN,
            "priority", 1
        );
        
        // Execute
        RuleEngineOutput output = ruleEngine.evaluateRuleWithData(1L, entityData);
        
        // Verify
        assertNotNull(output);
        assertEquals(1L, output.getRuleId());
        assertEquals("Test Rule", output.getRuleName());
        assertEquals(EntityType.TICKET, output.getEntityType());
        assertEquals(1L, output.getEntityId());
        assertTrue(output.isResult());
    }
    
    @Test
    void testEvaluateRuleWithData_RuleNotFound() {
        // Setup mocks
        when(ruleService.findById(anyLong())).thenReturn(Optional.empty());
        
        Map<String, Object> entityData = Map.of(
            "id", 1L,
            "title", "Test Ticket",
            "status", TicketStatus.OPEN,
            "priority", 1
        );
        
        // Execute
        RuleEngineOutput output = ruleEngine.evaluateRuleWithData(1L, entityData);
        
        // Verify
        assertNull(output);
    }
}
