package com.gs.ruleengine.dto;

import com.gs.ruleengine.model.ActionType;
import javax.validation.constraints.NotNull;

public class ActionConfigurationDto {
    private Long id;
    
    @NotNull(message = "Rule ID is required")
    private Long ruleId;
    
    @NotNull(message = "Action type is required")
    private ActionType actionType;
    
    private String name;
    private String description;
    
    @NotNull(message = "Configuration is required")
    private Object configuration;
    
    private boolean active = true;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
    
    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Object getConfiguration() { return configuration; }
    public void setConfiguration(Object configuration) { this.configuration = configuration; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
