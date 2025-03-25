package com.gs.ruleengine.engine;

import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.RuleEngineOutput;
import java.util.List;
import java.util.Map;

/**
 * Interface for the rule engine.
 */
public interface RuleEngine {
    
    /**
     * Evaluates a single rule against an entity.
     * 
     * @param ruleId The ID of the rule to evaluate
     * @param entityId The ID of the entity to evaluate against
     * @return The output of the rule evaluation
     */
    RuleEngineOutput evaluateRule(Long ruleId, Long entityId);
    
    /**
     * Evaluates all active rules for a specific entity type against an entity.
     * 
     * @param entityType The type of entity
     * @param entityId The ID of the entity to evaluate against
     * @return List of outputs from the rule evaluations
     */
    List<RuleEngineOutput> evaluateRules(EntityType entityType, Long entityId);
    
    /**
     * Evaluates a rule against entity data directly.
     * 
     * @param ruleId The ID of the rule to evaluate
     * @param entityData Map of entity field names to their values
     * @return The output of the rule evaluation
     */
    RuleEngineOutput evaluateRuleWithData(Long ruleId, Map<String, Object> entityData);
    
    /**
     * Evaluates all active rules for a specific entity type against entity data directly.
     * 
     * @param entityType The type of entity
     * @param entityData Map of entity field names to their values
     * @return List of outputs from the rule evaluations
     */
    List<RuleEngineOutput> evaluateRulesWithData(EntityType entityType, Map<String, Object> entityData);
}
