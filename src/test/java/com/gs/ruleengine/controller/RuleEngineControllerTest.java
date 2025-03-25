package com.gs.ruleengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ruleengine.dto.RuleExecutionRequest;
import com.gs.ruleengine.dto.RuleExecutionResponse;
import com.gs.ruleengine.engine.ActionEngine;
import com.gs.ruleengine.engine.RuleEngine;
import com.gs.ruleengine.model.ActionOutput;
import com.gs.ruleengine.model.ActionType;
import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.RuleEngineOutput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RuleEngineController.class)
class RuleEngineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RuleEngine ruleEngine;

    @MockBean
    private ActionEngine actionEngine;

    private RuleExecutionRequest request;
    private RuleEngineOutput ruleOutput;
    private ActionOutput actionOutput;

    @BeforeEach
    void setUp() {
        // Setup test request
        request = new RuleExecutionRequest();
        request.setEntityType(EntityType.TICKET);
        request.setEntityId(1L);
        
        // Setup test rule output
        ruleOutput = new RuleEngineOutput(1L, "Test Rule", EntityType.TICKET, 1L, true);
        
        // Setup test action output
        actionOutput = new ActionOutput(1L, "Test Action", ActionType.EMAIL, 1L, "Test Rule", 1L, EntityType.TICKET, true, "Action executed successfully");
    }

    @Test
    void executeRules_WithSpecificRules() throws Exception {
        request.setRuleIds(Arrays.asList(1L, 2L));
        
        when(ruleEngine.evaluateRule(1L, 1L)).thenReturn(ruleOutput);
        when(ruleEngine.evaluateRule(2L, 1L)).thenReturn(null);
        when(actionEngine.executeActions(anyList())).thenReturn(Collections.singletonList(actionOutput));

        mockMvc.perform(post("/api/rule-engine/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleEngineOutputs", hasSize(1)))
                .andExpect(jsonPath("$.ruleEngineOutputs[0].ruleId", is(1)))
                .andExpect(jsonPath("$.ruleEngineOutputs[0].ruleName", is("Test Rule")))
                .andExpect(jsonPath("$.ruleEngineOutputs[0].entityType", is("TICKET")))
                .andExpect(jsonPath("$.ruleEngineOutputs[0].entityId", is(1)))
                .andExpect(jsonPath("$.ruleEngineOutputs[0].result", is(true)))
                .andExpect(jsonPath("$.actionOutputs", hasSize(1)))
                .andExpect(jsonPath("$.actionOutputs[0].actionConfigurationId", is(1)))
                .andExpect(jsonPath("$.actionOutputs[0].actionName", is("Test Action")))
                .andExpect(jsonPath("$.actionOutputs[0].actionType", is("EMAIL")))
                .andExpect(jsonPath("$.actionOutputs[0].ruleId", is(1)))
                .andExpect(jsonPath("$.actionOutputs[0].ruleName", is("Test Rule")))
                .andExpect(jsonPath("$.actionOutputs[0].entityId", is(1)))
                .andExpect(jsonPath("$.actionOutputs[0].entityType", is("TICKET")))
                .andExpect(jsonPath("$.actionOutputs[0].success", is(true)))
                .andExpect(jsonPath("$.actionOutputs[0].message", is("Action executed successfully")));
    }

    @Test
    void executeRules_AllRulesForEntityType() throws Exception {
        List<RuleEngineOutput> ruleOutputs = Collections.singletonList(ruleOutput);
        
        when(ruleEngine.evaluateRules(EntityType.TICKET, 1L)).thenReturn(ruleOutputs);
        when(actionEngine.executeActions(ruleOutputs)).thenReturn(Collections.singletonList(actionOutput));

        mockMvc.perform(post("/api/rule-engine/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleEngineOutputs", hasSize(1)))
                .andExpect(jsonPath("$.ruleEngineOutputs[0].ruleId", is(1)))
                .andExpect(jsonPath("$.actionOutputs", hasSize(1)))
                .andExpect(jsonPath("$.actionOutputs[0].actionConfigurationId", is(1)));
    }

    @Test
    void executeRules_NoRulesFound() throws Exception {
        when(ruleEngine.evaluateRules(any(), anyLong())).thenReturn(Collections.emptyList());
        when(actionEngine.executeActions(anyList())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/rule-engine/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleEngineOutputs", hasSize(0)))
                .andExpect(jsonPath("$.actionOutputs", hasSize(0)));
    }
}
