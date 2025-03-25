package com.gs.ruleengine.engine.action;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handler for property update actions.
 */
@Component
public class PropertyUpdateActionHandler implements ActionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(PropertyUpdateActionHandler.class);
    
    private final ObjectMapper objectMapper;
    private final TicketService ticketService;
    private final RosterService rosterService;
    private final LeaveService leaveService;
    
    @Autowired
    public PropertyUpdateActionHandler(
            ObjectMapper objectMapper,
            TicketService ticketService,
            RosterService rosterService,
            LeaveService leaveService) {
        this.objectMapper = objectMapper;
        this.ticketService = ticketService;
        this.rosterService = rosterService;
        this.leaveService = leaveService;
    }
    
    @Override
    public ActionOutput execute(RuleEngineOutput ruleEngineOutput, ActionConfiguration actionConfiguration, Map<String, Object> entityData) {
        if (!ruleEngineOutput.isResult()) {
            logger.info("Rule result is false, skipping property update action for rule: {}", ruleEngineOutput.getRuleId());
            return new ActionOutput(
                    actionConfiguration.getId(),
                    actionConfiguration.getName(),
                    actionConfiguration.getActionType(),
                    ruleEngineOutput.getRuleId(),
                    ruleEngineOutput.getRuleName(),
                    ruleEngineOutput.getEntityId(),
                    ruleEngineOutput.getEntityType(),
                    true,
                    "Action skipped as rule result is false"
            );
        }
        
        try {
            PropertyUpdateActionConfig config = objectMapper.readValue(actionConfiguration.getConfigurationJson(), PropertyUpdateActionConfig.class);
            
            // In a real implementation, this would update entity properties
            // For now, we'll just log the property updates
            logger.info("Updating properties for entity: {} with ID: {}", ruleEngineOutput.getEntityType(), ruleEngineOutput.getEntityId());
            logger.info("Properties to update: {}", config.getPropertiesToUpdate());
            
            // Simulate updating entity properties
            boolean updated = updateEntityProperties(
                    ruleEngineOutput.getEntityType(),
                    ruleEngineOutput.getEntityId(),
                    config.getPropertiesToUpdate()
            );
            
            if (updated) {
                return new ActionOutput(
                        actionConfiguration.getId(),
                        actionConfiguration.getName(),
                        actionConfiguration.getActionType(),
                        ruleEngineOutput.getRuleId(),
                        ruleEngineOutput.getRuleName(),
                        ruleEngineOutput.getEntityId(),
                        ruleEngineOutput.getEntityType(),
                        true,
                        "Properties updated successfully"
                );
            } else {
                return new ActionOutput(
                        actionConfiguration.getId(),
                        actionConfiguration.getName(),
                        actionConfiguration.getActionType(),
                        ruleEngineOutput.getRuleId(),
                        ruleEngineOutput.getRuleName(),
                        ruleEngineOutput.getEntityId(),
                        ruleEngineOutput.getEntityType(),
                        false,
                        "Failed to update entity properties"
                );
            }
        } catch (JsonProcessingException e) {
            logger.error("Error parsing property update action configuration: {}", actionConfiguration.getConfigurationJson(), e);
            
            return new ActionOutput(
                    actionConfiguration.getId(),
                    actionConfiguration.getName(),
                    actionConfiguration.getActionType(),
                    ruleEngineOutput.getRuleId(),
                    ruleEngineOutput.getRuleName(),
                    ruleEngineOutput.getEntityId(),
                    ruleEngineOutput.getEntityType(),
                    false,
                    "Failed to parse property update action configuration: " + e.getMessage()
            );
        } catch (Exception e) {
            logger.error("Error executing property update action: {}", actionConfiguration.getId(), e);
            
            return new ActionOutput(
                    actionConfiguration.getId(),
                    actionConfiguration.getName(),
                    actionConfiguration.getActionType(),
                    ruleEngineOutput.getRuleId(),
                    ruleEngineOutput.getRuleName(),
                    ruleEngineOutput.getEntityId(),
                    ruleEngineOutput.getEntityType(),
                    false,
                    "Failed to execute property update action: " + e.getMessage()
            );
        }
    }
    
    @Override
    public boolean canHandle(ActionConfiguration actionConfiguration) {
        return actionConfiguration.getActionType() == ActionType.PROPERTY_UPDATE;
    }
    
    /**
     * Updates entity properties.
     * 
     * @param entityType The type of entity
     * @param entityId The ID of the entity
     * @param properties The properties to update
     * @return true if the update was successful, false otherwise
     */
    private boolean updateEntityProperties(EntityType entityType, Long entityId, Map<String, Object> properties) {
        try {
            if (entityType == EntityType.TICKET) {
                return ticketService.findById(entityId)
                        .map(ticket -> {
                            // Update ticket properties
                            if (properties.containsKey("assignee")) {
                                ticket.setAssignee((String) properties.get("assignee"));
                            }
                            // Add other property updates as needed
                            
                            // Save the updated ticket
                            ticketService.save(ticket);
                            return true;
                        })
                        .orElse(false);
            } else if (entityType == EntityType.ROSTER) {
                // Handle roster updates
                return true;
            } else if (entityType == EntityType.LEAVE) {
                // Handle leave updates
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error updating entity properties: {}", e.getMessage(), e);
            return false;
        }
    }
}
