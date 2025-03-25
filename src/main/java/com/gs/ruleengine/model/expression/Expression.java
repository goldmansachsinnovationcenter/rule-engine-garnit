package com.gs.ruleengine.model.expression;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Map;

/**
 * Interface for all expressions in the rule engine.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Condition.class, name = "CONDITION"),
    @JsonSubTypes.Type(value = AndExpression.class, name = "AND"),
    @JsonSubTypes.Type(value = OrExpression.class, name = "OR")
})
public interface Expression {
    
    /**
     * Evaluates the expression against the provided entity data.
     * 
     * @param entityData Map of entity field names to their values
     * @return true if the expression evaluates to true, false otherwise
     */
    boolean evaluate(Map<String, Object> entityData);
}
