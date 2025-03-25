package com.gs.ruleengine.service;

import com.gs.ruleengine.model.Ticket;
import com.gs.ruleengine.repository.TicketRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TicketService {
    
    private final TicketRepository ticketRepository;
    
    @Autowired
    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }
    
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }
    
    public Optional<Ticket> findById(Long id) {
        return ticketRepository.findById(id);
    }
    
    public Ticket save(Ticket ticket) {
        return ticketRepository.save(ticket);
    }
    
    public void deleteById(Long id) {
        ticketRepository.deleteById(id);
    }
}
