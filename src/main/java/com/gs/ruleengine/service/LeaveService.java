package com.gs.ruleengine.service;

import com.gs.ruleengine.model.Leave;
import com.gs.ruleengine.repository.LeaveRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LeaveService {
    
    private final LeaveRepository leaveRepository;
    
    @Autowired
    public LeaveService(LeaveRepository leaveRepository) {
        this.leaveRepository = leaveRepository;
    }
    
    public List<Leave> findAll() {
        return leaveRepository.findAll();
    }
    
    public Optional<Leave> findById(Long id) {
        return leaveRepository.findById(id);
    }
    
    public Leave save(Leave leave) {
        return leaveRepository.save(leave);
    }
    
    public void deleteById(Long id) {
        leaveRepository.deleteById(id);
    }
}
