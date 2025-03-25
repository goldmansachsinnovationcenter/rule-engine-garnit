package com.gs.ruleengine.model.expression;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a leaf condition in the expression tree.
 */
public class Condition implements Expression {
    
    private String field;
    private Operator operator;
    private Object value;
    
    @JsonCreator
    public Condition(
            @JsonProperty("field") String field,
            @JsonProperty("operator") Operator operator,
            @JsonProperty("value") Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }
    
    @Override
    public boolean evaluate(Map<String, Object> entityData) {
        Object fieldValue = entityData.get(field);
        
        if (operator == Operator.IS_NULL) {
            return fieldValue == null;
        }
        
        if (operator == Operator.IS_NOT_NULL) {
            return fieldValue != null;
        }
        
        if (fieldValue == null) {
            return false;
        }
        
        switch (operator) {
            case EQUALS:
                return Objects.equals(fieldValue, value);
            case NOT_EQUALS:
                return !Objects.equals(fieldValue, value);
            case GREATER_THAN:
                return compareValues(fieldValue, value) > 0;
            case GREATER_THAN_OR_EQUALS:
                return compareValues(fieldValue, value) >= 0;
            case LESS_THAN:
                return compareValues(fieldValue, value) < 0;
            case LESS_THAN_OR_EQUALS:
                return compareValues(fieldValue, value) <= 0;
            case CONTAINS:
                return fieldValue.toString().contains(value.toString());
            case STARTS_WITH:
                return fieldValue.toString().startsWith(value.toString());
            case ENDS_WITH:
                return fieldValue.toString().endsWith(value.toString());
            default:
                return false;
        }
    }
    
    @SuppressWarnings("unchecked")
    private int compareValues(Object o1, Object o2) {
        if (o1 instanceof Comparable && o2 instanceof Comparable) {
            return ((Comparable<Object>) o1).compareTo(o2);
        }
        return 0;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public Operator getOperator() {
        return operator;
    }
    
    public void setOperator(Operator operator) {
        this.operator = operator;
    }
    
    public Object getValue() {
        return value;
    }
    
    public void setValue(Object value) {
        this.value = value;
    }
}
