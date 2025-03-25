package com.gs.ruleengine.model;

import java.time.LocalDateTime;

/**
 * Output from the action engine after executing an action.
 */
public class ActionOutput {
    
    private Long actionConfigurationId;
    private String actionName;
    private ActionType actionType;
    private Long ruleId;
    private String ruleName;
    private Long entityId;
    private EntityType entityType;
    private boolean success;
    private String message;
    private LocalDateTime executionTime;
    
    public ActionOutput() {
        this.executionTime = LocalDateTime.now();
    }
    
    public ActionOutput(Long actionConfigurationId, String actionName, ActionType actionType, 
                        Long ruleId, String ruleName, Long entityId, EntityType entityType, 
                        boolean success, String message) {
        this.actionConfigurationId = actionConfigurationId;
        this.actionName = actionName;
        this.actionType = actionType;
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.entityId = entityId;
        this.entityType = entityType;
        this.success = success;
        this.message = message;
        this.executionTime = LocalDateTime.now();
    }
    
    public Long getActionConfigurationId() {
        return actionConfigurationId;
    }
    
    public void setActionConfigurationId(Long actionConfigurationId) {
        this.actionConfigurationId = actionConfigurationId;
    }
    
    public String getActionName() {
        return actionName;
    }
    
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    
    public ActionType getActionType() {
        return actionType;
    }
    
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
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
    
    public Long getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    
    public EntityType getEntityType() {
        return entityType;
    }
    
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }
}
