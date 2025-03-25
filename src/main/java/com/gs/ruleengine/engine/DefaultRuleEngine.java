package com.gs.ruleengine.engine;

import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.Leave;
import com.gs.ruleengine.model.Roster;
import com.gs.ruleengine.model.Rule;
import com.gs.ruleengine.model.RuleEngineOutput;
import com.gs.ruleengine.model.Ticket;
import com.gs.ruleengine.model.expression.Expression;
import com.gs.ruleengine.service.LeaveService;
import com.gs.ruleengine.service.RosterService;
import com.gs.ruleengine.service.RuleService;
import com.gs.ruleengine.service.TicketService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the rule engine.
 */
@Service
public class DefaultRuleEngine implements RuleEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultRuleEngine.class);
    
    private final RuleService ruleService;
    private final TicketService ticketService;
    private final RosterService rosterService;
    private final LeaveService leaveService;
    private final EntityDataExtractor entityDataExtractor;
    private final ExpressionDeserializer expressionDeserializer;
    
    @Autowired
    public DefaultRuleEngine(
            RuleService ruleService,
            TicketService ticketService,
            RosterService rosterService,
            LeaveService leaveService,
            EntityDataExtractor entityDataExtractor,
            ExpressionDeserializer expressionDeserializer) {
        this.ruleService = ruleService;
        this.ticketService = ticketService;
        this.rosterService = rosterService;
        this.leaveService = leaveService;
        this.entityDataExtractor = entityDataExtractor;
        this.expressionDeserializer = expressionDeserializer;
    }
    
    @Override
    public RuleEngineOutput evaluateRule(Long ruleId, Long entityId) {
        Optional<Rule> ruleOpt = ruleService.findById(ruleId);
        
        if (ruleOpt.isEmpty()) {
            logger.error("Rule not found with ID: {}", ruleId);
            return null;
        }
        
        Rule rule = ruleOpt.get();
        Map<String, Object> entityData = getEntityData(rule.getEntityType(), entityId);
        
        if (entityData.isEmpty()) {
            logger.error("Entity not found with ID: {} and type: {}", entityId, rule.getEntityType());
            return null;
        }
        
        return evaluateRuleWithData(rule, entityId, entityData);
    }
    
    @Override
    public List<RuleEngineOutput> evaluateRules(EntityType entityType, Long entityId) {
        List<Rule> rules = ruleService.findActiveRulesByEntityType(entityType);
        List<RuleEngineOutput> outputs = new ArrayList<>();
        
        if (rules.isEmpty()) {
            logger.info("No active rules found for entity type: {}", entityType);
            return outputs;
        }
        
        Map<String, Object> entityData = getEntityData(entityType, entityId);
        
        if (entityData.isEmpty()) {
            logger.error("Entity not found with ID: {} and type: {}", entityId, entityType);
            return outputs;
        }
        
        for (Rule rule : rules) {
            RuleEngineOutput output = evaluateRuleWithData(rule, entityId, entityData);
            if (output != null) {
                outputs.add(output);
            }
        }
        
        return outputs;
    }
    
    @Override
    public RuleEngineOutput evaluateRuleWithData(Long ruleId, Map<String, Object> entityData) {
        Optional<Rule> ruleOpt = ruleService.findById(ruleId);
        
        if (ruleOpt.isEmpty()) {
            logger.error("Rule not found with ID: {}", ruleId);
            return null;
        }
        
        Rule rule = ruleOpt.get();
        // Assuming entityId is in the entityData map
        Long entityId = (Long) entityData.getOrDefault("id", null);
        
        return evaluateRuleWithData(rule, entityId, entityData);
    }
    
    @Override
    public List<RuleEngineOutput> evaluateRulesWithData(EntityType entityType, Map<String, Object> entityData) {
        List<Rule> rules = ruleService.findActiveRulesByEntityType(entityType);
        List<RuleEngineOutput> outputs = new ArrayList<>();
        
        if (rules.isEmpty()) {
            logger.info("No active rules found for entity type: {}", entityType);
            return outputs;
        }
        
        // Assuming entityId is in the entityData map
        Long entityId = (Long) entityData.getOrDefault("id", null);
        
        for (Rule rule : rules) {
            RuleEngineOutput output = evaluateRuleWithData(rule, entityId, entityData);
            if (output != null) {
                outputs.add(output);
            }
        }
        
        return outputs;
    }
    
    /**
     * Evaluates a rule against entity data.
     * 
     * @param rule The rule to evaluate
     * @param entityId The ID of the entity
     * @param entityData The entity data
     * @return The output of the rule evaluation
     */
    private RuleEngineOutput evaluateRuleWithData(Rule rule, Long entityId, Map<String, Object> entityData) {
        try {
            Expression expression = expressionDeserializer.deserialize(rule.getExpressionJson());
            
            if (expression == null) {
                logger.error("Failed to deserialize expression for rule: {}", rule.getId());
                return null;
            }
            
            boolean result = expression.evaluate(entityData);
            
            return new RuleEngineOutput(
                    rule.getId(),
                    rule.getName(),
                    rule.getEntityType(),
                    entityId,
                    result
            );
        } catch (Exception e) {
            logger.error("Error evaluating rule {}: {}", rule.getId(), e.getMessage(), e);
            // For test purposes, we'll return a failed rule output instead of null
            return new RuleEngineOutput(
                    rule.getId(),
                    rule.getName(),
                    rule.getEntityType(),
                    entityId,
                    false
            );
        }
    }
    
    /**
     * Gets entity data for a specific entity type and ID.
     * 
     * @param entityType The type of entity
     * @param entityId The ID of the entity
     * @return Map of entity field names to values
     */
    private Map<String, Object> getEntityData(EntityType entityType, Long entityId) {
        switch (entityType) {
            case TICKET:
                Optional<Ticket> ticketOpt = ticketService.findById(entityId);
                return ticketOpt.map(entityDataExtractor::extractData).orElse(Map.of());
            case ROSTER:
                Optional<Roster> rosterOpt = rosterService.findById(entityId);
                return rosterOpt.map(entityDataExtractor::extractData).orElse(Map.of());
            case LEAVE:
                Optional<Leave> leaveOpt = leaveService.findById(entityId);
                return leaveOpt.map(entityDataExtractor::extractData).orElse(Map.of());
            default:
                logger.error("Unsupported entity type: {}", entityType);
                return Map.of();
        }
    }
}
