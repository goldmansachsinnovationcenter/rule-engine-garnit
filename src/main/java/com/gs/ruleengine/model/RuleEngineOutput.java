package com.gs.ruleengine.model;

import java.time.LocalDateTime;

/**
 * Output from the rule engine after evaluating a rule against an entity.
 */
public class RuleEngineOutput {
    
    private Long ruleId;
    private String ruleName;
    private EntityType entityType;
    private Long entityId;
    private boolean result;
    private LocalDateTime evaluationTime;
    
    public RuleEngineOutput() {
        this.evaluationTime = LocalDateTime.now();
    }
    
    public RuleEngineOutput(Long ruleId, String ruleName, EntityType entityType, Long entityId, boolean result) {
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.entityType = entityType;
        this.entityId = entityId;
        this.result = result;
        this.evaluationTime = LocalDateTime.now();
    }
    
    public Long getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public EntityType getEntityType() {
        return entityType;
    }
    
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public boolean isResult() {
        return result;
    }
    
    public void setResult(boolean result) {
        this.result = result;
    }
    
    public LocalDateTime getEvaluationTime() {
        return evaluationTime;
    }
    
    public void setEvaluationTime(LocalDateTime evaluationTime) {
        this.evaluationTime = evaluationTime;
    }
}
