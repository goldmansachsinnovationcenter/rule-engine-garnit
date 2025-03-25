package com.gs.ruleengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gs.ruleengine.dto.ActionConfigurationDto;
import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.service.ActionConfigurationService;
import com.gs.ruleengine.service.RuleService;
import javax.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/actions/configurations")
public class ActionConfigurationController {
    
    private final ActionConfigurationService actionConfigurationService;
    private final RuleService ruleService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public ActionConfigurationController(
            ActionConfigurationService actionConfigurationService,
            RuleService ruleService,
            ObjectMapper objectMapper) {
        this.actionConfigurationService = actionConfigurationService;
        this.ruleService = ruleService;
        this.objectMapper = objectMapper;
    }
    
    @GetMapping
    public ResponseEntity<List<ActionConfiguration>> getAllActionConfigurations() {
        return ResponseEntity.ok(actionConfigurationService.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ActionConfiguration> getActionConfigurationById(@PathVariable Long id) {
        return actionConfigurationService.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                        "Action configuration not found with ID: " + id));
    }
    
    @GetMapping("/rule/{ruleId}")
    public ResponseEntity<List<ActionConfiguration>> getActionConfigurationsByRuleId(@PathVariable Long ruleId) {
        if (!ruleService.findById(ruleId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found with ID: " + ruleId);
        }
        return ResponseEntity.ok(actionConfigurationService.findByRuleId(ruleId));
    }
    
    @PostMapping
    public ResponseEntity<ActionConfiguration> createActionConfiguration(
            @Valid @RequestBody ActionConfigurationDto dto) {
        if (!ruleService.findById(dto.getRuleId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found with ID: " + dto.getRuleId());
        }
        
        try {
            ActionConfiguration config = new ActionConfiguration();
            config.setRuleId(dto.getRuleId());
            config.setActionType(dto.getActionType());
            config.setName(dto.getName());
            config.setDescription(dto.getDescription());
            config.setActive(dto.isActive());
            
            // Serialize configuration to JSON
            String configJson = objectMapper.writeValueAsString(dto.getConfiguration());
            config.setConfigurationJson(configJson);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(actionConfigurationService.save(config));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Error creating action configuration: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ActionConfiguration> updateActionConfiguration(
            @PathVariable Long id, @Valid @RequestBody ActionConfigurationDto dto) {
        if (!actionConfigurationService.findById(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Action configuration not found with ID: " + id);
        }
        
        if (!ruleService.findById(dto.getRuleId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule not found with ID: " + dto.getRuleId());
        }
        
        try {
            ActionConfiguration config = actionConfigurationService.findById(id).get();
            config.setRuleId(dto.getRuleId());
            config.setActionType(dto.getActionType());
            config.setName(dto.getName());
            config.setDescription(dto.getDescription());
            config.setActive(dto.isActive());
            
            // Serialize configuration to JSON
            String configJson = objectMapper.writeValueAsString(dto.getConfiguration());
            config.setConfigurationJson(configJson);
            
            return ResponseEntity.ok(actionConfigurationService.save(config));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Error updating action configuration: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActionConfiguration(@PathVariable Long id) {
        if (!actionConfigurationService.findById(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Action configuration not found with ID: " + id);
        }
        
        actionConfigurationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
