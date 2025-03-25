package com.gs.ruleengine.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.gs.ruleengine.model.expression.AndExpression;
import com.gs.ruleengine.model.expression.Condition;
import com.gs.ruleengine.model.expression.Expression;
import com.gs.ruleengine.model.expression.OrExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility for deserializing expression JSON to expression objects.
 */
@Component
public class ExpressionDeserializer {
    
    private static final Logger logger = LoggerFactory.getLogger(ExpressionDeserializer.class);
    
    private final ObjectMapper objectMapper;
    
    public ExpressionDeserializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerSubtypes(
            new NamedType(AndExpression.class, "AND"),
            new NamedType(OrExpression.class, "OR"),
            new NamedType(Condition.class, "CONDITION")
        );
    }
    
    /**
     * Deserializes expression JSON to an expression object.
     * 
     * @param expressionJson The JSON representation of the expression
     * @return The deserialized expression object
     */
    public Expression deserialize(String expressionJson) {
        try {
            return objectMapper.readValue(expressionJson, Expression.class);
        } catch (JsonProcessingException e) {
            logger.error("Error deserializing expression JSON: {}", expressionJson, e);
            return null;
        }
    }
    
    /**
     * Serializes an expression object to JSON.
     * 
     * @param expression The expression object
     * @return The JSON representation of the expression
     */
    public String serialize(Expression expression) {
        try {
            return objectMapper.writeValueAsString(expression);
        } catch (JsonProcessingException e) {
            logger.error("Error serializing expression: {}", expression, e);
            return null;
        }
    }
}
