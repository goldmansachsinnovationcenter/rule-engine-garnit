package com.gs.ruleengine.engine.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.model.ActionOutput;
import com.gs.ruleengine.model.ActionType;
import com.gs.ruleengine.model.RuleEngineOutput;
import com.gs.ruleengine.model.action.EmailActionConfig;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler for email actions.
 */
@Component
public class EmailActionHandler implements ActionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailActionHandler.class);
    
    private final ObjectMapper objectMapper;
    
    public EmailActionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public ActionOutput execute(RuleEngineOutput ruleEngineOutput, ActionConfiguration actionConfiguration, Map<String, Object> entityData) {
        if (!ruleEngineOutput.isResult()) {
            logger.info("Rule result is false, skipping email action for rule: {}", ruleEngineOutput.getRuleId());
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
            EmailActionConfig config = objectMapper.readValue(actionConfiguration.getConfigurationJson(), EmailActionConfig.class);
            
            // In a real implementation, this would send an email
            // For now, we'll just log the email details
            logger.info("Sending email to: {}", config.getRecipients());
            logger.info("Email subject: {}", config.getSubject());
            logger.info("Email template: {}", config.getTemplate());
            
            if (config.isIncludeEntityDetails()) {
                logger.info("Entity details: {}", entityData);
            }
            
            return new ActionOutput(
                    actionConfiguration.getId(),
                    actionConfiguration.getName(),
                    actionConfiguration.getActionType(),
                    ruleEngineOutput.getRuleId(),
                    ruleEngineOutput.getRuleName(),
                    ruleEngineOutput.getEntityId(),
                    ruleEngineOutput.getEntityType(),
                    true,
                    "Email sent successfully"
            );
        } catch (JsonProcessingException e) {
            logger.error("Error parsing email action configuration: {}", actionConfiguration.getConfigurationJson(), e);
            
            return new ActionOutput(
                    actionConfiguration.getId(),
                    actionConfiguration.getName(),
                    actionConfiguration.getActionType(),
                    ruleEngineOutput.getRuleId(),
                    ruleEngineOutput.getRuleName(),
                    ruleEngineOutput.getEntityId(),
                    ruleEngineOutput.getEntityType(),
                    false,
                    "Failed to parse email action configuration: " + e.getMessage()
            );
        } catch (Exception e) {
            logger.error("Error executing email action: {}", actionConfiguration.getId(), e);
            
            return new ActionOutput(
                    actionConfiguration.getId(),
                    actionConfiguration.getName(),
                    actionConfiguration.getActionType(),
                    ruleEngineOutput.getRuleId(),
                    ruleEngineOutput.getRuleName(),
                    ruleEngineOutput.getEntityId(),
                    ruleEngineOutput.getEntityType(),
                    false,
                    "Failed to execute email action: " + e.getMessage()
            );
        }
    }
    
    @Override
    public boolean canHandle(ActionConfiguration actionConfiguration) {
        return actionConfiguration.getActionType() == ActionType.EMAIL;
    }
}
