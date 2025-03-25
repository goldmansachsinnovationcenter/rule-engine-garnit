package com.gs.ruleengine.engine.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.model.ActionOutput;
import com.gs.ruleengine.model.ActionType;
import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.RuleEngineOutput;
import com.gs.ruleengine.model.action.PropertyUpdateActionConfig;
import com.gs.ruleengine.service.LeaveService;
import com.gs.ruleengine.service.RosterService;
import com.gs.ruleengine.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyUpdateActionHandlerTest {

    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private TicketService ticketService;
    
    @Mock
    private RosterService rosterService;
    
    @Mock
    private LeaveService leaveService;
    
    @InjectMocks
    private PropertyUpdateActionHandler propertyUpdateActionHandler;
    
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
        actionConfiguration.setName("Property Update Action");
        actionConfiguration.setActionType(ActionType.PROPERTY_UPDATE);
        actionConfiguration.setConfigurationJson("{\"propertiesToUpdate\":{\"status\":\"IN_PROGRESS\",\"priority\":2}}");
        
        // Setup entity data
        entityData = new HashMap<>();
        entityData.put("id", 1L);
        entityData.put("title", "Test Ticket");
        entityData.put("status", "OPEN");
        entityData.put("priority", 1);
    }
    
    @Test
    void testCanHandle() {
        assertTrue(propertyUpdateActionHandler.canHandle(actionConfiguration));
        
        ActionConfiguration nonPropertyUpdateAction = new ActionConfiguration();
        nonPropertyUpdateAction.setActionType(ActionType.EMAIL);
        assertFalse(propertyUpdateActionHandler.canHandle(nonPropertyUpdateAction));
    }
    
    @Test
    void testExecute_Success() throws Exception {
        // Setup mock
        PropertyUpdateActionConfig config = new PropertyUpdateActionConfig();
        Map<String, Object> propertiesToUpdate = new HashMap<>();
        propertiesToUpdate.put("status", "IN_PROGRESS");
        propertiesToUpdate.put("priority", 2);
        config.setPropertiesToUpdate(propertiesToUpdate);
        
        when(objectMapper.readValue(anyString(), eq(PropertyUpdateActionConfig.class))).thenReturn(config);
        
        // Execute
        ActionOutput output = propertyUpdateActionHandler.execute(ruleEngineOutput, actionConfiguration, entityData);
        
        // Verify
        assertNotNull(output);
        assertEquals(1L, output.getActionConfigurationId());
        assertEquals("Property Update Action", output.getActionName());
        assertEquals(ActionType.PROPERTY_UPDATE, output.getActionType());
        assertEquals(1L, output.getRuleId());
        assertEquals("Test Rule", output.getRuleName());
        assertEquals(1L, output.getEntityId());
        assertEquals(EntityType.TICKET, output.getEntityType());
        assertTrue(output.isSuccess());
        assertEquals("Properties updated successfully", output.getMessage());
    }
    
    @Test
    void testExecute_RuleResultFalse() throws Exception {
        // Setup rule engine output with false result
        RuleEngineOutput falseRuleOutput = new RuleEngineOutput(1L, "Test Rule", EntityType.TICKET, 1L, false);
        
        // Execute
        ActionOutput output = propertyUpdateActionHandler.execute(falseRuleOutput, actionConfiguration, entityData);
        
        // Verify
        assertNotNull(output);
        assertEquals(1L, output.getActionConfigurationId());
        assertEquals("Property Update Action", output.getActionName());
        assertEquals(ActionType.PROPERTY_UPDATE, output.getActionType());
        assertEquals(1L, output.getRuleId());
        assertEquals("Test Rule", output.getRuleName());
        assertEquals(1L, output.getEntityId());
        assertEquals(EntityType.TICKET, output.getEntityType());
        assertTrue(output.isSuccess());
        assertEquals("Action skipped as rule result is false", output.getMessage());
    }
}
