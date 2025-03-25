package com.gs.ruleengine.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "action_configurations")
public class ActionConfiguration extends BaseEntity {
    
    @NotNull(message = "Rule ID is required")
    private Long ruleId;
    
    @NotNull(message = "Action type is required")
    @Enumerated(EnumType.STRING)
    private ActionType actionType;
    
    private String name;
    
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String configurationJson;
    
    private boolean active = true;
    
    public ActionConfiguration() {
        super();
    }
    
    public Long getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }
    
    public ActionType getActionType() {
        return actionType;
    }
    
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getConfigurationJson() {
        return configurationJson;
    }
    
    public void setConfigurationJson(String configurationJson) {
        this.configurationJson = configurationJson;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
