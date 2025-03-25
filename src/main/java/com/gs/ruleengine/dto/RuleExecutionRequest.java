package com.gs.ruleengine.dto;

import com.gs.ruleengine.model.EntityType;
import javax.validation.constraints.NotNull;
import java.util.List;

public class RuleExecutionRequest {
    @NotNull(message = "Entity type is required")
    private EntityType entityType;
    
    @NotNull(message = "Entity ID is required")
    private Long entityId;
    
    private List<Long> ruleIds;
    
    // Getters and setters
    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }
    
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    
    public List<Long> getRuleIds() { return ruleIds; }
    public void setRuleIds(List<Long> ruleIds) { this.ruleIds = ruleIds; }
}
