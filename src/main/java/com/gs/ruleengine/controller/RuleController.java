package com.gs.ruleengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ruleengine.dto.RuleDto;
import com.gs.ruleengine.engine.ExpressionDeserializer;
import com.gs.ruleengine.model.Rule;
import com.gs.ruleengine.service.RuleService;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/rules")
public class RuleController {
    
    private final RuleService ruleService;
    private final ExpressionDeserializer expressionDeserializer;
    
    @Autowired
    public RuleController(RuleService ruleService, ExpressionDeserializer expressionDeserializer) {
        this.ruleService = ruleService;
        this.expressionDeserializer = expressionDeserializer;
    }
    
    @GetMapping
    public ResponseEntity<List<Rule>> getAllRules() {
        return ResponseEntity.ok(ruleService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Rule> getRuleById(@PathVariable Long id) {
        return ruleService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found with ID: " + id));
    }
    
    @PostMapping
    public ResponseEntity<Rule> createRule(@Valid @RequestBody RuleDto ruleDto) {
        try {
            Rule rule = new Rule();
            rule.setName(ruleDto.getName());
            rule.setEntityType(ruleDto.getEntityType());
            rule.setDescription(ruleDto.getDescription());
            rule.setActive(ruleDto.isActive());
            
            // Serialize expression to JSON
            String expressionJson = expressionDeserializer.serialize(ruleDto.getExpression());
            rule.setExpressionJson(expressionJson);
            
            Rule savedRule = ruleService.save(rule);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRule);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error creating rule: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Rule> updateRule(@PathVariable Long id, @Valid @RequestBody RuleDto ruleDto) {
        return ruleService.findById(id)
                .map(rule -> {
                    try {
                        rule.setName(ruleDto.getName());
                        rule.setEntityType(ruleDto.getEntityType());
                        rule.setDescription(ruleDto.getDescription());
                        rule.setActive(ruleDto.isActive());
                        
                        // Serialize expression to JSON
                        String expressionJson = expressionDeserializer.serialize(ruleDto.getExpression());
                        rule.setExpressionJson(expressionJson);
                        
                        return ResponseEntity.ok(ruleService.save(rule));
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error updating rule: " + e.getMessage());
                    }
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found with ID: " + id));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        if (!ruleService.findById(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found with ID: " + id);
        }
        ruleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
