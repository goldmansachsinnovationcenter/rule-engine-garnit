package com.gs.ruleengine.repository;

import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.Rule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Long> {
    
    List<Rule> findByEntityTypeAndActiveTrue(EntityType entityType);
}
