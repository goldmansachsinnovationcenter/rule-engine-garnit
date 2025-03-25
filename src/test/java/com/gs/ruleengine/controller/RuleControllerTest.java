package com.gs.ruleengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ruleengine.dto.RuleDto;
import com.gs.ruleengine.engine.ExpressionDeserializer;
import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.Rule;
import com.gs.ruleengine.model.expression.Condition;
import com.gs.ruleengine.model.expression.Expression;
import com.gs.ruleengine.model.expression.Operator;
import com.gs.ruleengine.service.RuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RuleController.class)
class RuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RuleService ruleService;

    @MockBean
    private ExpressionDeserializer expressionDeserializer;

    private Rule testRule;
    private RuleDto testRuleDto;
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
        
        // Setup test expression
        testExpression = new Condition("status", Operator.EQUALS, "OPEN");
        
        testExpressionJson = "{\"type\":\"CONDITION\",\"field\":\"status\",\"operator\":\"EQUALS\",\"value\":\"OPEN\"}";
        testRule.setExpressionJson(testExpressionJson);
        
        // Setup test rule DTO
        testRuleDto = new RuleDto();
        testRuleDto.setName("Test Rule");
        testRuleDto.setEntityType(EntityType.TICKET);
        testRuleDto.setActive(true);
        testRuleDto.setExpression(testExpression);
    }

    @Test
    void getAllRules() throws Exception {
        List<Rule> rules = Arrays.asList(testRule);
        when(ruleService.findAll()).thenReturn(rules);

        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Test Rule")))
                .andExpect(jsonPath("$[0].entityType", is("TICKET")))
                .andExpect(jsonPath("$[0].active", is(true)));
    }

    @Test
    void getRuleById() throws Exception {
        when(ruleService.findById(1L)).thenReturn(Optional.of(testRule));

        mockMvc.perform(get("/api/rules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Rule")))
                .andExpect(jsonPath("$.entityType", is("TICKET")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void getRuleById_NotFound() throws Exception {
        when(ruleService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/rules/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createRule() throws Exception {
        when(expressionDeserializer.serialize(any(Expression.class))).thenReturn(testExpressionJson);
        
        // Mock the save method to return the test rule with the correct properties
        when(ruleService.save(any(Rule.class))).thenAnswer(invocation -> {
            Rule savedRule = invocation.getArgument(0);
            savedRule.setId(1L); // Set the ID that would be assigned by the database
            return savedRule;
        });

        mockMvc.perform(post("/api/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRuleDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Rule")))
                .andExpect(jsonPath("$.entityType", is("TICKET")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void updateRule() throws Exception {
        when(ruleService.findById(1L)).thenReturn(Optional.of(testRule));
        when(expressionDeserializer.serialize(any(Expression.class))).thenReturn(testExpressionJson);
        
        // Mock the save method to return the updated rule
        when(ruleService.save(any(Rule.class))).thenAnswer(invocation -> {
            Rule savedRule = invocation.getArgument(0);
            return savedRule; // Return the updated rule
        });

        mockMvc.perform(put("/api/rules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRuleDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test Rule")))
                .andExpect(jsonPath("$.entityType", is("TICKET")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void updateRule_NotFound() throws Exception {
        when(ruleService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/rules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRuleDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRule() throws Exception {
        when(ruleService.findById(1L)).thenReturn(Optional.of(testRule));

        mockMvc.perform(delete("/api/rules/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteRule_NotFound() throws Exception {
        when(ruleService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/rules/1"))
                .andExpect(status().isNotFound());
    }
}
