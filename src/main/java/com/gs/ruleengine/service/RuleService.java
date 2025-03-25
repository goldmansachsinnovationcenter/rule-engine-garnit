package com.gs.ruleengine.service;

import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.Rule;
import com.gs.ruleengine.repository.RuleRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RuleService {
    
    private final RuleRepository ruleRepository;
    
    @Autowired
    public RuleService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }
    
    public List<Rule> findAll() {
        return ruleRepository.findAll();
    }
    
    public Optional<Rule> findById(Long id) {
        return ruleRepository.findById(id);
    }
    
    public List<Rule> findActiveRulesByEntityType(EntityType entityType) {
        return ruleRepository.findByEntityTypeAndActiveTrue(entityType);
    }
    
    public Rule save(Rule rule) {
        return ruleRepository.save(rule);
    }
    
    public void deleteById(Long id) {
        ruleRepository.deleteById(id);
    }
}
