package com.gs.ruleengine.service;

import com.gs.ruleengine.model.Roster;
import com.gs.ruleengine.repository.RosterRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RosterService {
    
    private final RosterRepository rosterRepository;
    
    @Autowired
    public RosterService(RosterRepository rosterRepository) {
        this.rosterRepository = rosterRepository;
    }
    
    public List<Roster> findAll() {
        return rosterRepository.findAll();
    }
    
    public Optional<Roster> findById(Long id) {
        return rosterRepository.findById(id);
    }
    
    public Roster save(Roster roster) {
        return rosterRepository.save(roster);
    }
    
    public void deleteById(Long id) {
        rosterRepository.deleteById(id);
    }
}
