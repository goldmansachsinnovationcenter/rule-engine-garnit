package com.gs.ruleengine.engine.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.model.ActionOutput;
import com.gs.ruleengine.model.ActionType;
import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.RuleEngineOutput;
import com.gs.ruleengine.model.action.EmailActionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailActionHandlerTest {

    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private EmailActionHandler emailActionHandler;
    
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
        actionConfiguration.setName("Email Action");
        actionConfiguration.setActionType(ActionType.EMAIL);
        actionConfiguration.setConfigurationJson("{\"recipients\":[\"test@example.com\"],\"subject\":\"Test Subject\",\"template\":\"Test Template\",\"includeEntityDetails\":true}");
        
        // Setup entity data
        entityData = Map.of(
            "id", 1L,
            "title", "Test Ticket",
            "status", "OPEN",
            "priority", 1
        );
    }
    
    @Test
    void testCanHandle() {
        assertTrue(emailActionHandler.canHandle(actionConfiguration));
        
        ActionConfiguration nonEmailAction = new ActionConfiguration();
        nonEmailAction.setActionType(ActionType.AGGREGATION);
        assertFalse(emailActionHandler.canHandle(nonEmailAction));
    }
    
    @Test
    void testExecute_Success() throws Exception {
        // Setup mock
        EmailActionConfig config = new EmailActionConfig();
        config.setRecipients(Arrays.asList("test@example.com"));
        config.setSubject("Test Subject");
        config.setTemplate("Test Template");
        config.setIncludeEntityDetails(true);
        
        when(objectMapper.readValue(anyString(), eq(EmailActionConfig.class))).thenReturn(config);
        
        // Execute
        ActionOutput output = emailActionHandler.execute(ruleEngineOutput, actionConfiguration, entityData);
        
        // Verify
        assertNotNull(output);
        assertEquals(1L, output.getActionConfigurationId());
        assertEquals("Email Action", output.getActionName());
        assertEquals(ActionType.EMAIL, output.getActionType());
        assertEquals(1L, output.getRuleId());
        assertEquals("Test Rule", output.getRuleName());
        assertEquals(1L, output.getEntityId());
        assertEquals(EntityType.TICKET, output.getEntityType());
        assertTrue(output.isSuccess());
        assertEquals("Email sent successfully", output.getMessage());
    }
    
    @Test
    void testExecute_RuleResultFalse() throws Exception {
        // Setup rule engine output with false result
        RuleEngineOutput falseRuleOutput = new RuleEngineOutput(1L, "Test Rule", EntityType.TICKET, 1L, false);
        
        // Execute
        ActionOutput output = emailActionHandler.execute(falseRuleOutput, actionConfiguration, entityData);
        
        // Verify
        assertNotNull(output);
        assertEquals(1L, output.getActionConfigurationId());
        assertEquals("Email Action", output.getActionName());
        assertEquals(ActionType.EMAIL, output.getActionType());
        assertEquals(1L, output.getRuleId());
        assertEquals("Test Rule", output.getRuleName());
        assertEquals(1L, output.getEntityId());
        assertEquals(EntityType.TICKET, output.getEntityType());
        assertTrue(output.isSuccess());
        assertEquals("Action skipped as rule result is false", output.getMessage());
    }
}
