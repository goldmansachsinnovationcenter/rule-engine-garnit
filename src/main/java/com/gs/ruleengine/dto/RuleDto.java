package com.gs.ruleengine.dto;

import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.expression.Expression;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class RuleDto {
    private Long id;
    
    @NotBlank(message = "Rule name is required")
    private String name;
    
    @NotNull(message = "Entity type is required")
    private EntityType entityType;
    
    private String description;
    
    @NotNull(message = "Expression is required")
    private Expression expression;
    
    private boolean active = true;
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Expression getExpression() { return expression; }
    public void setExpression(Expression expression) { this.expression = expression; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
