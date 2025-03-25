package com.gs.ruleengine.engine.action;

import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.model.ActionOutput;
import com.gs.ruleengine.model.RuleEngineOutput;
import java.util.Map;

/**
 * Interface for handling different action types.
 */
public interface ActionHandler {
    
    /**
     * Executes an action based on the rule engine output and action configuration.
     * 
     * @param ruleEngineOutput The output from the rule engine
     * @param actionConfiguration The action configuration
     * @param entityData The entity data
     * @return The output of the action execution
     */
    ActionOutput execute(RuleEngineOutput ruleEngineOutput, ActionConfiguration actionConfiguration, Map<String, Object> entityData);
    
    /**
     * Checks if this handler can handle the given action configuration.
     * 
     * @param actionConfiguration The action configuration
     * @return true if this handler can handle the action configuration, false otherwise
     */
    boolean canHandle(ActionConfiguration actionConfiguration);
}
