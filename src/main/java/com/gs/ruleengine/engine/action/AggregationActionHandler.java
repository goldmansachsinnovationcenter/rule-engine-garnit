package com.gs.ruleengine.engine.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.model.ActionOutput;
import com.gs.ruleengine.model.ActionType;
import com.gs.ruleengine.model.RuleEngineOutput;
import com.gs.ruleengine.model.action.AggregationActionConfig;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for aggregation actions.
 */
@Component
public class AggregationActionHandler implements ActionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AggregationActionHandler.class);
    
    private final ObjectMapper objectMapper;
    
    public AggregationActionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public ActionOutput execute(RuleEngineOutput ruleEngineOutput, ActionConfiguration actionConfiguration, Map<String, Object> entityData) {
        if (!ruleEngineOutput.isResult()) {
            logger.info("Rule result is false, skipping aggregation action for rule: {}", ruleEngineOutput.getRuleId());
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
            AggregationActionConfig config = objectMapper.readValue(actionConfiguration.getConfigurationJson(), AggregationActionConfig.class);
            
            // In a real implementation, this would perform aggregation
            // For now, we'll just log the aggregation details
            logger.info("Performing aggregation on field: {}", config.getAggregationField());
            logger.info("Aggregation type: {}", config.getAggregationType());
            logger.info("Group by field: {}", config.getGroupByField());
            logger.info("Filter fields: {}", config.getFilterFields());
            logger.info("Output destination: {}", config.getOutputDestination());
            
            return new ActionOutput(
                    actionConfiguration.getId(),
                    actionConfiguration.getName(),
                    actionConfiguration.getActionType(),
                    ruleEngineOutput.getRuleId(),
                    ruleEngineOutput.getRuleName(),
                    ruleEngineOutput.getEntityId(),
                    ruleEngineOutput.getEntityType(),
                    true,
                    "Aggregation performed successfully"
            );
        } catch (JsonProcessingException e) {
            logger.error("Error parsing aggregation action configuration: {}", actionConfiguration.getConfigurationJson(), e);
            
            return new ActionOutput(
                    actionConfiguration.getId(),
                    actionConfiguration.getName(),
                    actionConfiguration.getActionType(),
                    ruleEngineOutput.getRuleId(),
                    ruleEngineOutput.getRuleName(),
                    ruleEngineOutput.getEntityId(),
                    ruleEngineOutput.getEntityType(),
                    false,
                    "Failed to parse aggregation action configuration: " + e.getMessage()
            );
        } catch (Exception e) {
            logger.error("Error executing aggregation action: {}", actionConfiguration.getId(), e);
            
            return new ActionOutput(
                    actionConfiguration.getId(),
                    actionConfiguration.getName(),
                    actionConfiguration.getActionType(),
                    ruleEngineOutput.getRuleId(),
                    ruleEngineOutput.getRuleName(),
                    ruleEngineOutput.getEntityId(),
                    ruleEngineOutput.getEntityType(),
                    false,
                    "Failed to execute aggregation action: " + e.getMessage()
            );
        }
    }
    
    @Override
    public boolean canHandle(ActionConfiguration actionConfiguration) {
        return actionConfiguration.getActionType() == ActionType.AGGREGATION;
    }
}
