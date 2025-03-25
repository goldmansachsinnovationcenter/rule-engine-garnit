package com.gs.ruleengine.model;

import com.gs.ruleengine.model.expression.Expression;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "rules")
public class Rule extends BaseEntity {
    
    @NotBlank(message = "Rule name is required")
    private String name;
    
    @NotNull(message = "Entity type is required")
    @Enumerated(EnumType.STRING)
    private EntityType entityType;
    
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String expressionJson;
    
    private boolean active = true;
    
    public Rule() {
        super();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public EntityType getEntityType() {
        return entityType;
    }
    
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getExpressionJson() {
        return expressionJson;
    }
    
    public void setExpressionJson(String expressionJson) {
        this.expressionJson = expressionJson;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
