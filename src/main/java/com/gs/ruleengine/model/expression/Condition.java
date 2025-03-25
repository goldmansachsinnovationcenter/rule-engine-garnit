package com.gs.ruleengine.model.expression;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a leaf condition in the expression tree.
 */
public class Condition implements Expression {
    
    private static final Logger logger = LoggerFactory.getLogger(Condition.class);
    
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
        
        logger.info("Evaluating condition: field={}, operator={}, value={}, fieldValue={}", 
                field, operator, value, fieldValue);
        
        if (operator == Operator.IS_NULL) {
            return fieldValue == null;
        }
        
        if (operator == Operator.IS_NOT_NULL) {
            return fieldValue != null;
        }
        
        if (fieldValue == null) {
            logger.info("Field value is null, returning false");
            return false;
        }
        
        // Handle enum comparison - convert string value to enum if field value is an enum
        if (fieldValue instanceof Enum && value instanceof String) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Enum> enumClass = ((Enum) fieldValue).getClass();
                Object enumValue = Enum.valueOf(enumClass, (String) value);
                logger.info("Converting string value '{}' to enum value '{}' for comparison", value, enumValue);
                value = enumValue;
            } catch (IllegalArgumentException e) {
                logger.warn("Failed to convert string '{}' to enum type of {}", value, fieldValue.getClass().getName());
                return false;
            }
        }
        
        // Handle numeric comparison - convert string value to number if field value is a number
        if (fieldValue instanceof Number && value instanceof String) {
            try {
                if (fieldValue instanceof Integer) {
                    value = Integer.valueOf((String) value);
                    logger.info("Converting string value '{}' to Integer for comparison", value);
                } else if (fieldValue instanceof Long) {
                    value = Long.valueOf((String) value);
                    logger.info("Converting string value '{}' to Long for comparison", value);
                } else if (fieldValue instanceof Double) {
                    value = Double.valueOf((String) value);
                    logger.info("Converting string value '{}' to Double for comparison", value);
                }
            } catch (NumberFormatException e) {
                logger.warn("Failed to convert string '{}' to numeric type of {}", value, fieldValue.getClass().getName());
                return false;
            }
        }
        
        boolean result = false;
        switch (operator) {
            case EQUALS:
                result = Objects.equals(fieldValue, value);
                break;
            case NOT_EQUALS:
                result = !Objects.equals(fieldValue, value);
                break;
            case GREATER_THAN:
                result = compareValues(fieldValue, value) > 0;
                break;
            case GREATER_THAN_OR_EQUALS:
                result = compareValues(fieldValue, value) >= 0;
                break;
            case LESS_THAN:
                result = compareValues(fieldValue, value) < 0;
                break;
            case LESS_THAN_OR_EQUALS:
                result = compareValues(fieldValue, value) <= 0;
                break;
            case CONTAINS:
                result = fieldValue.toString().contains(value.toString());
                break;
            case STARTS_WITH:
                result = fieldValue.toString().startsWith(value.toString());
                break;
            case ENDS_WITH:
                result = fieldValue.toString().endsWith(value.toString());
                break;
            default:
                result = false;
                break;
        }
        
        logger.info("Condition evaluation result: {}", result);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private int compareValues(Object o1, Object o2) {
        if (o1 instanceof Comparable && o2 instanceof Comparable) {
            try {
                return ((Comparable<Object>) o1).compareTo(o2);
            } catch (ClassCastException e) {
                logger.warn("Cannot compare {} with {}: {}", o1, o2, e.getMessage());
                return 0;
            }
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
