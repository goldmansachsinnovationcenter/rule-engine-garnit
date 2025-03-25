package com.gs.ruleengine.engine.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.model.ActionOutput;
import com.gs.ruleengine.model.ActionType;
import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.RuleEngineOutput;
import com.gs.ruleengine.model.action.AggregationActionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AggregationActionHandlerTest {

    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private AggregationActionHandler aggregationActionHandler;
    
    private RuleEngineOutput ruleEngineOutput;
    private ActionConfiguration actionConfiguration;
    private Map<String, Object> entityData;
    
    @BeforeEach
    void setUp() throws Exception {
        // Setup rule engine output
        ruleEngineOutput = new RuleEngineOutput(1L, "Test Rule", EntityType.TICKET, 1L, true);
        
        // Setup action configuration
        actionConfiguration = new ActionConfiguration();
        actionConfiguration.setId(1L);
        actionConfiguration.setRuleId(1L);
        actionConfiguration.setName("Aggregation Action");
        actionConfiguration.setActionType(ActionType.AGGREGATION);
        actionConfiguration.setConfigurationJson("{\"aggregationField\":\"priority\",\"aggregationType\":\"AVG\",\"groupByField\":\"status\",\"filterFields\":[\"assignee\"],\"outputDestination\":\"DB\"}");
        
        // Setup entity data
        entityData = new HashMap<>();
        entityData.put("id", 1L);
        entityData.put("title", "Test Ticket");
        entityData.put("status", "OPEN");
        entityData.put("priority", 1);
        entityData.put("assignee", "John");
    }
    
    @Test
    void testCanHandle() {
        assertTrue(aggregationActionHandler.canHandle(actionConfiguration));
        
        ActionConfiguration nonAggregationAction = new ActionConfiguration();
        nonAggregationAction.setActionType(ActionType.EMAIL);
        assertFalse(aggregationActionHandler.canHandle(nonAggregationAction));
    }
    
    @Test
    void testExecute_Success() throws Exception {
        // Setup mock
        AggregationActionConfig config = new AggregationActionConfig();
        config.setAggregationField("priority");
        config.setAggregationType("AVG");
        config.setGroupByField("status");
        config.setFilterFields(Arrays.asList("assignee"));
        config.setOutputDestination("DB");
        
        when(objectMapper.readValue(anyString(), eq(AggregationActionConfig.class))).thenReturn(config);
        
        // Execute
        ActionOutput output = aggregationActionHandler.execute(ruleEngineOutput, actionConfiguration, entityData);
        
        // Verify
        assertNotNull(output);
        assertEquals(1L, output.getActionConfigurationId());
        assertEquals("Aggregation Action", output.getActionName());
        assertEquals(ActionType.AGGREGATION, output.getActionType());
        assertEquals(1L, output.getRuleId());
        assertEquals("Test Rule", output.getRuleName());
        assertEquals(1L, output.getEntityId());
        assertEquals(EntityType.TICKET, output.getEntityType());
        assertTrue(output.isSuccess());
        assertEquals("Aggregation performed successfully", output.getMessage());
    }
    
    @Test
    void testExecute_RuleResultFalse() throws Exception {
        // Setup rule engine output with false result
        RuleEngineOutput falseRuleOutput = new RuleEngineOutput(1L, "Test Rule", EntityType.TICKET, 1L, false);
        
        // Execute
        ActionOutput output = aggregationActionHandler.execute(falseRuleOutput, actionConfiguration, entityData);
        
        // Verify
        assertNotNull(output);
        assertEquals(1L, output.getActionConfigurationId());
        assertEquals("Aggregation Action", output.getActionName());
        assertEquals(ActionType.AGGREGATION, output.getActionType());
        assertEquals(1L, output.getRuleId());
        assertEquals("Test Rule", output.getRuleName());
        assertEquals(1L, output.getEntityId());
        assertEquals(EntityType.TICKET, output.getEntityType());
        assertTrue(output.isSuccess());
        assertEquals("Action skipped as rule result is false", output.getMessage());
    }
}
