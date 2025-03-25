package com.gs.ruleengine.service;

import com.gs.ruleengine.model.ActionConfiguration;
import com.gs.ruleengine.repository.ActionConfigurationRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionConfigurationService {
    
    private final ActionConfigurationRepository actionConfigurationRepository;
    
    @Autowired
    public ActionConfigurationService(ActionConfigurationRepository actionConfigurationRepository) {
        this.actionConfigurationRepository = actionConfigurationRepository;
    }
    
    public List<ActionConfiguration> findAll() {
        return actionConfigurationRepository.findAll();
    }
    
    public Optional<ActionConfiguration> findById(Long id) {
        return actionConfigurationRepository.findById(id);
    }
    
    public List<ActionConfiguration> findByRuleId(Long ruleId) {
        return actionConfigurationRepository.findByRuleIdAndActiveTrue(ruleId);
    }
    
    public ActionConfiguration save(ActionConfiguration actionConfiguration) {
        return actionConfigurationRepository.save(actionConfiguration);
    }
    
    public void deleteById(Long id) {
        actionConfigurationRepository.deleteById(id);
    }
}
