package com.gs.ruleengine.repository;

import com.gs.ruleengine.model.ActionConfiguration;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionConfigurationRepository extends JpaRepository<ActionConfiguration, Long> {
    
    List<ActionConfiguration> findByRuleIdAndActiveTrue(Long ruleId);
}
