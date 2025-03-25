package com.gs.ruleengine.engine;

import com.gs.ruleengine.model.ActionOutput;
import com.gs.ruleengine.model.RuleEngineOutput;
import java.util.List;

/**
 * Interface for the action engine.
 */
public interface ActionEngine {
    
    /**
     * Executes actions for a rule engine output.
     * 
     * @param ruleEngineOutput The output from the rule engine
     * @return List of outputs from the action executions
     */
    List<ActionOutput> executeActions(RuleEngineOutput ruleEngineOutput);
    
    /**
     * Executes actions for multiple rule engine outputs.
     * 
     * @param ruleEngineOutputs List of outputs from the rule engine
     * @return List of outputs from the action executions
     */
    List<ActionOutput> executeActions(List<RuleEngineOutput> ruleEngineOutputs);
}
