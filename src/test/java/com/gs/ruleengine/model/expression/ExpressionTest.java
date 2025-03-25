package com.gs.ruleengine.model.expression;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ExpressionTest {

    @Test
    void testConditionEquals() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "OPEN");
        
        Condition condition = new Condition("status", Operator.EQUALS, "OPEN");
        assertTrue(condition.evaluate(data));
        
        condition = new Condition("status", Operator.EQUALS, "CLOSED");
        assertFalse(condition.evaluate(data));
    }
    
    @Test
    void testConditionNotEquals() {
        Map<String, Object> data = new HashMap<>();
        data.put("priority", 1);
        
        Condition condition = new Condition("priority", Operator.NOT_EQUALS, 2);
        assertTrue(condition.evaluate(data));
        
        condition = new Condition("priority", Operator.NOT_EQUALS, 1);
        assertFalse(condition.evaluate(data));
    }
    
    @Test
    void testConditionGreaterThan() {
        Map<String, Object> data = new HashMap<>();
        data.put("priority", 5);
        
        Condition condition = new Condition("priority", Operator.GREATER_THAN, 3);
        assertTrue(condition.evaluate(data));
        
        condition = new Condition("priority", Operator.GREATER_THAN, 5);
        assertFalse(condition.evaluate(data));
        
        condition = new Condition("priority", Operator.GREATER_THAN, 7);
        assertFalse(condition.evaluate(data));
    }
    
    @Test
    void testConditionLessThan() {
        Map<String, Object> data = new HashMap<>();
        data.put("priority", 2);
        
        Condition condition = new Condition("priority", Operator.LESS_THAN, 5);
        assertTrue(condition.evaluate(data));
        
        condition = new Condition("priority", Operator.LESS_THAN, 2);
        assertFalse(condition.evaluate(data));
        
        condition = new Condition("priority", Operator.LESS_THAN, 1);
        assertFalse(condition.evaluate(data));
    }
    
    @Test
    void testConditionContains() {
        Map<String, Object> data = new HashMap<>();
        data.put("description", "This is a test description");
        
        Condition condition = new Condition("description", Operator.CONTAINS, "test");
        assertTrue(condition.evaluate(data));
        
        condition = new Condition("description", Operator.CONTAINS, "not found");
        assertFalse(condition.evaluate(data));
    }
    
    @Test
    void testAndExpression() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "OPEN");
        data.put("priority", 1);
        
        Condition condition1 = new Condition("status", Operator.EQUALS, "OPEN");
        Condition condition2 = new Condition("priority", Operator.EQUALS, 1);
        
        AndExpression andExpression = new AndExpression(Arrays.asList(condition1, condition2));
        assertTrue(andExpression.evaluate(data));
        
        // Change one condition to make it false
        data.put("priority", 2);
        assertFalse(andExpression.evaluate(data));
    }
    
    @Test
    void testOrExpression() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "CLOSED");
        data.put("priority", 3);
        
        Condition condition1 = new Condition("status", Operator.EQUALS, "OPEN");
        Condition condition2 = new Condition("priority", Operator.GREATER_THAN, 2);
        
        OrExpression orExpression = new OrExpression(Arrays.asList(condition1, condition2));
        assertTrue(orExpression.evaluate(data));
        
        // Change both conditions to make it false
        data.put("status", "CLOSED");
        data.put("priority", 1);
        assertFalse(orExpression.evaluate(data));
    }
    
    @Test
    void testComplexExpression() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "OPEN");
        data.put("priority", 1);
        data.put("assignee", "John");
        data.put("department", "IT");
        
        // (status = OPEN AND priority = 1) OR (assignee = John AND department = IT)
        Condition condition1 = new Condition("status", Operator.EQUALS, "OPEN");
        Condition condition2 = new Condition("priority", Operator.EQUALS, 1);
        Condition condition3 = new Condition("assignee", Operator.EQUALS, "John");
        Condition condition4 = new Condition("department", Operator.EQUALS, "IT");
        
        AndExpression andExpression1 = new AndExpression(Arrays.asList(condition1, condition2));
        AndExpression andExpression2 = new AndExpression(Arrays.asList(condition3, condition4));
        
        OrExpression orExpression = new OrExpression(Arrays.asList(andExpression1, andExpression2));
        
        assertTrue(orExpression.evaluate(data));
        
        // Make first AND expression false
        data.put("priority", 2);
        // Second AND expression is still true
        assertTrue(orExpression.evaluate(data));
        
        // Make second AND expression false too
        data.put("department", "HR");
        // Both AND expressions are false, so OR expression is false
        assertFalse(orExpression.evaluate(data));
    }
    
    @Test
    void testNullValues() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", null);
        
        Condition condition = new Condition("status", Operator.IS_NULL, null);
        assertTrue(condition.evaluate(data));
        
        condition = new Condition("status", Operator.IS_NOT_NULL, null);
        assertFalse(condition.evaluate(data));
        
        data.put("status", "OPEN");
        condition = new Condition("status", Operator.IS_NULL, null);
        assertFalse(condition.evaluate(data));
        
        condition = new Condition("status", Operator.IS_NOT_NULL, null);
        assertTrue(condition.evaluate(data));
    }
    
    @Test
    void testMissingField() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "OPEN");
        
        // Field not in data map
        Condition condition = new Condition("nonExistentField", Operator.EQUALS, "value");
        assertFalse(condition.evaluate(data));
    }
}
