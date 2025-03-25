package com.gs.ruleengine.model.expression;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Represents an OR expression in the expression tree.
 */
public class OrExpression implements Expression {
    
    private List<Expression> expressions;
    
    @JsonCreator
    public OrExpression(@JsonProperty("expressions") List<Expression> expressions) {
        this.expressions = expressions;
    }
    
    @Override
    public boolean evaluate(Map<String, Object> entityData) {
        if (expressions == null || expressions.isEmpty()) {
            return false;
        }
        
        for (Expression expression : expressions) {
            if (expression.evaluate(entityData)) {
                return true;
            }
        }
        
        return false;
    }
    
    public List<Expression> getExpressions() {
        return expressions;
    }
    
    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }
}
