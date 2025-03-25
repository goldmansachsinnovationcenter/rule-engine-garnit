package com.gs.ruleengine.controller;

import com.gs.ruleengine.dto.RuleExecutionRequest;
import com.gs.ruleengine.dto.RuleExecutionResponse;
import com.gs.ruleengine.engine.ActionEngine;
import com.gs.ruleengine.engine.RuleEngine;
import com.gs.ruleengine.model.ActionOutput;
import com.gs.ruleengine.model.RuleEngineOutput;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rule-engine")
public class RuleEngineController {
    
    private final RuleEngine ruleEngine;
    private final ActionEngine actionEngine;
    
    @Autowired
    public RuleEngineController(RuleEngine ruleEngine, ActionEngine actionEngine) {
        this.ruleEngine = ruleEngine;
        this.actionEngine = actionEngine;
    }
    
    @PostMapping("/execute")
    public ResponseEntity<RuleExecutionResponse> executeRules(@Valid @RequestBody RuleExecutionRequest request) {
        List<RuleEngineOutput> ruleEngineOutputs;
        
        if (request.getRuleIds() != null && !request.getRuleIds().isEmpty()) {
            // Execute specific rules
            ruleEngineOutputs = new ArrayList<>();
            
            for (Long ruleId : request.getRuleIds()) {
                RuleEngineOutput output = ruleEngine.evaluateRule(ruleId, request.getEntityId());
                if (output != null) {
                    ruleEngineOutputs.add(output);
                }
            }
        } else {
            // Execute all active rules for the entity type
            ruleEngineOutputs = ruleEngine.evaluateRules(request.getEntityType(), request.getEntityId());
        }
        
        // Execute actions for rule engine outputs
        List<ActionOutput> actionOutputs = actionEngine.executeActions(ruleEngineOutputs);
        
        RuleExecutionResponse response = new RuleExecutionResponse(ruleEngineOutputs, actionOutputs);
        return ResponseEntity.ok(response);
    }
}
