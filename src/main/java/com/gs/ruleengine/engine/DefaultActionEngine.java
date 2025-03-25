package com.gs.ruleengine.engine;

import com.gs.ruleengine.engine.action.ActionHandler;
import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.model.ActionOutput;
import com.gs.ruleengine.model.Leave;
import com.gs.ruleengine.model.Roster;
import com.gs.ruleengine.model.RuleEngineOutput;
import com.gs.ruleengine.model.Ticket;
import com.gs.ruleengine.service.ActionConfigurationService;
import com.gs.ruleengine.service.LeaveService;
import com.gs.ruleengine.service.RosterService;
import com.gs.ruleengine.service.TicketService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the action engine.
 */
@Service
public class DefaultActionEngine implements ActionEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultActionEngine.class);
    
    private final ActionConfigurationService actionConfigurationService;
    private final TicketService ticketService;
    private final RosterService rosterService;
    private final LeaveService leaveService;
    private final EntityDataExtractor entityDataExtractor;
    private final List<ActionHandler> actionHandlers;
    
    @Autowired
    public DefaultActionEngine(
            ActionConfigurationService actionConfigurationService,
            TicketService ticketService,
            RosterService rosterService,
            LeaveService leaveService,
            EntityDataExtractor entityDataExtractor,
            List<ActionHandler> actionHandlers) {
        this.actionConfigurationService = actionConfigurationService;
        this.ticketService = ticketService;
        this.rosterService = rosterService;
        this.leaveService = leaveService;
        this.entityDataExtractor = entityDataExtractor;
        this.actionHandlers = actionHandlers;
    }
    
    @Override
    public List<ActionOutput> executeActions(RuleEngineOutput ruleEngineOutput) {
        List<ActionConfiguration> actionConfigurations = actionConfigurationService.findByRuleId(ruleEngineOutput.getRuleId());
        List<ActionOutput> actionOutputs = new ArrayList<>();
        
        if (actionConfigurations.isEmpty()) {
            logger.info("No action configurations found for rule: {}", ruleEngineOutput.getRuleId());
            return actionOutputs;
        }
        
        Map<String, Object> entityData = getEntityData(ruleEngineOutput.getEntityType(), ruleEngineOutput.getEntityId());
        
        if (entityData.isEmpty()) {
            logger.error("Entity not found with ID: {} and type: {}", ruleEngineOutput.getEntityId(), ruleEngineOutput.getEntityType());
            return actionOutputs;
        }
        
        for (ActionConfiguration actionConfiguration : actionConfigurations) {
            ActionHandler handler = findHandler(actionConfiguration);
            
            if (handler == null) {
                logger.error("No handler found for action type: {}", actionConfiguration.getActionType());
                continue;
            }
            
            try {
                ActionOutput actionOutput = handler.execute(ruleEngineOutput, actionConfiguration, entityData);
                if (actionOutput != null) {
                    actionOutputs.add(actionOutput);
                }
            } catch (Exception e) {
                logger.error("Error executing action: {}", e.getMessage(), e);
                // Create an error action output
                ActionOutput errorOutput = new ActionOutput(
                    actionConfiguration.getId(),
                    actionConfiguration.getName(),
                    actionConfiguration.getActionType(),
                    ruleEngineOutput.getRuleId(),
                    ruleEngineOutput.getRuleName(),
                    ruleEngineOutput.getEntityId(),
                    ruleEngineOutput.getEntityType(),
                    false,
                    "Error executing action: " + e.getMessage()
                );
                actionOutputs.add(errorOutput);
            }
        }
        
        return actionOutputs;
    }
    
    @Override
    public List<ActionOutput> executeActions(List<RuleEngineOutput> ruleEngineOutputs) {
        List<ActionOutput> actionOutputs = new ArrayList<>();
        
        for (RuleEngineOutput ruleEngineOutput : ruleEngineOutputs) {
            actionOutputs.addAll(executeActions(ruleEngineOutput));
        }
        
        return actionOutputs;
    }
    
    /**
     * Finds an action handler for the given action configuration.
     * 
     * @param actionConfiguration The action configuration
     * @return The action handler, or null if no handler is found
     */
    private ActionHandler findHandler(ActionConfiguration actionConfiguration) {
        for (ActionHandler handler : actionHandlers) {
            if (handler.canHandle(actionConfiguration)) {
                return handler;
            }
        }
        
        return null;
    }
    
    /**
     * Gets entity data for a specific entity type and ID.
     * 
     * @param entityType The type of entity
     * @param entityId The ID of the entity
     * @return Map of entity field names to values
     */
    private Map<String, Object> getEntityData(com.gs.ruleengine.model.EntityType entityType, Long entityId) {
        switch (entityType) {
            case TICKET:
                Optional<Ticket> ticketOpt = ticketService.findById(entityId);
                return ticketOpt.map(entityDataExtractor::extractData).orElse(Map.of());
            case ROSTER:
                Optional<Roster> rosterOpt = rosterService.findById(entityId);
                return rosterOpt.map(entityDataExtractor::extractData).orElse(Map.of());
            case LEAVE:
                Optional<Leave> leaveOpt = leaveService.findById(entityId);
                return leaveOpt.map(entityDataExtractor::extractData).orElse(Map.of());
            default:
                logger.error("Unsupported entity type: {}", entityType);
                return Map.of();
        }
    }
}
