package com.gs.ruleengine.engine;

import com.gs.ruleengine.engine.action.ActionHandler;
import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.model.ActionOutput;
import com.gs.ruleengine.model.ActionType;
import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.RuleEngineOutput;
import com.gs.ruleengine.model.Ticket;
import com.gs.ruleengine.service.ActionConfigurationService;
import com.gs.ruleengine.service.LeaveService;
import com.gs.ruleengine.service.RosterService;
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
class ActionEngineTest {

    @Mock
    private ActionConfigurationService actionConfigurationService;
    
    @Mock
    private TicketService ticketService;
    
    @Mock
    private RosterService rosterService;
    
    @Mock
    private LeaveService leaveService;
    
    @Mock
    private EntityDataExtractor entityDataExtractor;
    
    @Mock
    private ActionHandler emailActionHandler;
    
    @Mock
    private ActionHandler aggregationActionHandler;
    
    @Mock
    private ActionHandler propertyUpdateActionHandler;
    
    @InjectMocks
    private DefaultActionEngine actionEngine;
    
    private RuleEngineOutput ruleEngineOutput;
    private ActionConfiguration emailActionConfig;
    private ActionConfiguration aggregationActionConfig;
    private ActionConfiguration propertyUpdateActionConfig;
    private Ticket testTicket;
    
    @BeforeEach
    void setUp() {
        // Setup rule engine output
        ruleEngineOutput = new RuleEngineOutput(1L, "Test Rule", EntityType.TICKET, 1L, true);
        
        // Setup action configurations
        emailActionConfig = new ActionConfiguration();
        emailActionConfig.setId(1L);
        emailActionConfig.setRuleId(1L);
        emailActionConfig.setName("Email Action");
        emailActionConfig.setActionType(ActionType.EMAIL);
        emailActionConfig.setConfigurationJson("{\"recipients\":[\"test@example.com\"],\"subject\":\"Test Subject\",\"template\":\"Test Template\",\"includeEntityDetails\":true}");
        emailActionConfig.setActive(true);
        
        aggregationActionConfig = new ActionConfiguration();
        aggregationActionConfig.setId(2L);
        aggregationActionConfig.setRuleId(1L);
        aggregationActionConfig.setName("Aggregation Action");
        aggregationActionConfig.setActionType(ActionType.AGGREGATION);
        aggregationActionConfig.setConfigurationJson("{\"aggregationField\":\"priority\",\"aggregationType\":\"AVG\",\"groupByField\":\"status\",\"filterFields\":[\"assignee\"],\"outputDestination\":\"DB\"}");
        aggregationActionConfig.setActive(true);
        
        propertyUpdateActionConfig = new ActionConfiguration();
        propertyUpdateActionConfig.setId(3L);
        propertyUpdateActionConfig.setRuleId(1L);
        propertyUpdateActionConfig.setName("Property Update Action");
        propertyUpdateActionConfig.setActionType(ActionType.PROPERTY_UPDATE);
        propertyUpdateActionConfig.setConfigurationJson("{\"propertiesToUpdate\":{\"status\":\"IN_PROGRESS\",\"priority\":2}}");
        propertyUpdateActionConfig.setActive(true);
        
        // Setup test ticket
        testTicket = new Ticket();
        testTicket.setId(1L);
        testTicket.setTitle("Test Ticket");
    }
    
    @Test
    void testExecuteActions_SingleRuleOutput_Success() {
        // Setup mocks
        List<ActionConfiguration> actionConfigurations = Arrays.asList(
                emailActionConfig, aggregationActionConfig, propertyUpdateActionConfig);
        
        when(actionConfigurationService.findByRuleId(1L)).thenReturn(actionConfigurations);
        when(ticketService.findById(1L)).thenReturn(Optional.of(testTicket));
        
        Map<String, Object> ticketData = Map.of(
            "id", 1L,
            "title", "Test Ticket"
        );
        when(entityDataExtractor.extractData(testTicket)).thenReturn(ticketData);
        
        // Setup action handlers
        when(emailActionHandler.canHandle(emailActionConfig)).thenReturn(true);
        when(aggregationActionHandler.canHandle(aggregationActionConfig)).thenReturn(true);
        when(propertyUpdateActionHandler.canHandle(propertyUpdateActionConfig)).thenReturn(true);
        
        ActionOutput emailOutput = new ActionOutput(1L, "Email Action", ActionType.EMAIL, 1L, "Test Rule", 1L, EntityType.TICKET, true, "Email sent successfully");
        when(emailActionHandler.execute(ruleEngineOutput, emailActionConfig, ticketData)).thenReturn(emailOutput);
        
        ActionOutput aggregationOutput = new ActionOutput(2L, "Aggregation Action", ActionType.AGGREGATION, 1L, "Test Rule", 1L, EntityType.TICKET, true, "Aggregation performed successfully");
        when(aggregationActionHandler.execute(ruleEngineOutput, aggregationActionConfig, ticketData)).thenReturn(aggregationOutput);
        
        ActionOutput propertyUpdateOutput = new ActionOutput(3L, "Property Update Action", ActionType.PROPERTY_UPDATE, 1L, "Test Rule", 1L, EntityType.TICKET, true, "Properties updated successfully");
        when(propertyUpdateActionHandler.execute(ruleEngineOutput, propertyUpdateActionConfig, ticketData)).thenReturn(propertyUpdateOutput);
        
        // Set action handlers in the action engine
        actionEngine = new DefaultActionEngine(
                actionConfigurationService,
                ticketService,
                rosterService,
                leaveService,
                entityDataExtractor,
                Arrays.asList(emailActionHandler, aggregationActionHandler, propertyUpdateActionHandler)
        );
        
        // Execute
        List<ActionOutput> outputs = actionEngine.executeActions(ruleEngineOutput);
        
        // Verify
        assertNotNull(outputs);
        assertEquals(3, outputs.size());
        
        ActionOutput output1 = outputs.get(0);
        assertEquals(1L, output1.getActionConfigurationId());
        assertEquals("Email Action", output1.getActionName());
        assertEquals(ActionType.EMAIL, output1.getActionType());
        assertEquals(1L, output1.getRuleId());
        assertEquals("Test Rule", output1.getRuleName());
        assertEquals(1L, output1.getEntityId());
        assertEquals(EntityType.TICKET, output1.getEntityType());
        assertTrue(output1.isSuccess());
        assertEquals("Email sent successfully", output1.getMessage());
        
        ActionOutput output2 = outputs.get(1);
        assertEquals(2L, output2.getActionConfigurationId());
        assertEquals("Aggregation Action", output2.getActionName());
        assertEquals(ActionType.AGGREGATION, output2.getActionType());
        
        ActionOutput output3 = outputs.get(2);
        assertEquals(3L, output3.getActionConfigurationId());
        assertEquals("Property Update Action", output3.getActionName());
        assertEquals(ActionType.PROPERTY_UPDATE, output3.getActionType());
    }
    
    @Test
    void testExecuteActions_NoActionConfigurations() {
        // Setup mocks
        when(actionConfigurationService.findByRuleId(anyLong())).thenReturn(List.of());
        
        // Execute
        List<ActionOutput> outputs = actionEngine.executeActions(ruleEngineOutput);
        
        // Verify
        assertNotNull(outputs);
        assertTrue(outputs.isEmpty());
    }
    
    @Test
    void testExecuteActions_EntityNotFound() {
        // Setup mocks
        List<ActionConfiguration> actionConfigurations = List.of(emailActionConfig);
        
        when(actionConfigurationService.findByRuleId(1L)).thenReturn(actionConfigurations);
        when(ticketService.findById(anyLong())).thenReturn(Optional.empty());
        
        // Execute
        List<ActionOutput> outputs = actionEngine.executeActions(ruleEngineOutput);
        
        // Verify
        assertNotNull(outputs);
        assertTrue(outputs.isEmpty());
    }
    
    @Test
    void testExecuteActions_NoHandlerFound() {
        // Setup mocks
        List<ActionConfiguration> actionConfigurations = List.of(emailActionConfig);
        
        when(actionConfigurationService.findByRuleId(1L)).thenReturn(actionConfigurations);
        when(ticketService.findById(1L)).thenReturn(Optional.of(testTicket));
        
        Map<String, Object> ticketData = Map.of(
            "id", 1L,
            "title", "Test Ticket"
        );
        when(entityDataExtractor.extractData(testTicket)).thenReturn(ticketData);
        
        // Setup action handlers to not handle any action
        when(emailActionHandler.canHandle(any())).thenReturn(false);
        when(aggregationActionHandler.canHandle(any())).thenReturn(false);
        when(propertyUpdateActionHandler.canHandle(any())).thenReturn(false);
        
        // Set action handlers in the action engine
        actionEngine = new DefaultActionEngine(
                actionConfigurationService,
                ticketService,
                rosterService,
                leaveService,
                entityDataExtractor,
                Arrays.asList(emailActionHandler, aggregationActionHandler, propertyUpdateActionHandler)
        );
        
        // Execute
        List<ActionOutput> outputs = actionEngine.executeActions(ruleEngineOutput);
        
        // Verify
        assertNotNull(outputs);
        assertTrue(outputs.isEmpty());
    }
    
    @Test
    void testExecuteActions_MultipleRuleOutputs() {
        // Setup mocks
        RuleEngineOutput ruleOutput1 = new RuleEngineOutput(1L, "Rule 1", EntityType.TICKET, 1L, true);
        RuleEngineOutput ruleOutput2 = new RuleEngineOutput(2L, "Rule 2", EntityType.TICKET, 1L, false);
        
        List<RuleEngineOutput> ruleOutputs = Arrays.asList(ruleOutput1, ruleOutput2);
        
        ActionConfiguration actionConfig1 = new ActionConfiguration();
        actionConfig1.setId(1L);
        actionConfig1.setRuleId(1L);
        actionConfig1.setName("Action 1");
        actionConfig1.setActionType(ActionType.EMAIL);
        
        ActionConfiguration actionConfig2 = new ActionConfiguration();
        actionConfig2.setId(2L);
        actionConfig2.setRuleId(2L);
        actionConfig2.setName("Action 2");
        actionConfig2.setActionType(ActionType.PROPERTY_UPDATE);
        
        when(actionConfigurationService.findByRuleId(1L)).thenReturn(List.of(actionConfig1));
        when(actionConfigurationService.findByRuleId(2L)).thenReturn(List.of(actionConfig2));
        
        when(ticketService.findById(1L)).thenReturn(Optional.of(testTicket));
        
        Map<String, Object> ticketData = Map.of(
            "id", 1L,
            "title", "Test Ticket"
        );
        when(entityDataExtractor.extractData(testTicket)).thenReturn(ticketData);
        
        // Setup action handlers
        when(emailActionHandler.canHandle(actionConfig1)).thenReturn(true);
        when(propertyUpdateActionHandler.canHandle(actionConfig2)).thenReturn(true);
        
        ActionOutput actionOutput1 = new ActionOutput(1L, "Action 1", ActionType.EMAIL, 1L, "Rule 1", 1L, EntityType.TICKET, true, "Success");
        when(emailActionHandler.execute(ruleOutput1, actionConfig1, ticketData)).thenReturn(actionOutput1);
        
        ActionOutput actionOutput2 = new ActionOutput(2L, "Action 2", ActionType.PROPERTY_UPDATE, 2L, "Rule 2", 1L, EntityType.TICKET, false, "Skipped");
        when(propertyUpdateActionHandler.execute(ruleOutput2, actionConfig2, ticketData)).thenReturn(actionOutput2);
        
        // Set action handlers in the action engine
        actionEngine = new DefaultActionEngine(
                actionConfigurationService,
                ticketService,
                rosterService,
                leaveService,
                entityDataExtractor,
                Arrays.asList(emailActionHandler, propertyUpdateActionHandler)
        );
        
        // Execute
        List<ActionOutput> outputs = actionEngine.executeActions(ruleOutputs);
        
        // Verify
        assertNotNull(outputs);
        assertEquals(2, outputs.size());
        
        ActionOutput output1 = outputs.get(0);
        assertEquals(1L, output1.getActionConfigurationId());
        assertEquals("Action 1", output1.getActionName());
        assertEquals(ActionType.EMAIL, output1.getActionType());
        
        ActionOutput output2 = outputs.get(1);
        assertEquals(2L, output2.getActionConfigurationId());
        assertEquals("Action 2", output2.getActionName());
        assertEquals(ActionType.PROPERTY_UPDATE, output2.getActionType());
    }
}
