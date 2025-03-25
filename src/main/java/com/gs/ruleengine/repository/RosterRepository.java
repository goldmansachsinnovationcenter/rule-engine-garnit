package com.gs.ruleengine.repository;

import com.gs.ruleengine.model.Roster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RosterRepository extends JpaRepository<Roster, Long> {
}
