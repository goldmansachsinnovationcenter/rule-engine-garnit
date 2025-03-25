package com.gs.ruleengine.controller;

import com.gs.ruleengine.model.EntityType;
import com.gs.ruleengine.model.Leave;
import com.gs.ruleengine.model.Roster;
import com.gs.ruleengine.model.Ticket;
import com.gs.ruleengine.service.LeaveService;
import com.gs.ruleengine.service.RosterService;
import com.gs.ruleengine.service.TicketService;
import javax.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/entities")
public class EntityController {
    
    private final TicketService ticketService;
    private final RosterService rosterService;
    private final LeaveService leaveService;
    
    @Autowired
    public EntityController(
            TicketService ticketService,
            RosterService rosterService,
            LeaveService leaveService) {
        this.ticketService = ticketService;
        this.rosterService = rosterService;
        this.leaveService = leaveService;
    }
    
    @GetMapping("/{type}")
    public ResponseEntity<?> getAllEntities(@PathVariable String type) {
        try {
            EntityType entityType = EntityType.valueOf(type.toUpperCase());
            
            switch (entityType) {
                case TICKET:
                    return ResponseEntity.ok(ticketService.findAll());
                case ROSTER:
                    return ResponseEntity.ok(rosterService.findAll());
                case LEAVE:
                    return ResponseEntity.ok(leaveService.findAll());
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported entity type: " + type);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid entity type: " + type);
        }
    }
    
    @GetMapping("/{type}/{id}")
    public ResponseEntity<?> getEntityById(@PathVariable String type, @PathVariable Long id) {
        try {
            EntityType entityType = EntityType.valueOf(type.toUpperCase());
            
            switch (entityType) {
                case TICKET:
                    return ticketService.findById(id)
                            .map(ResponseEntity::ok)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                                    "Ticket not found with ID: " + id));
                case ROSTER:
                    return rosterService.findById(id)
                            .map(ResponseEntity::ok)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                                    "Roster not found with ID: " + id));
                case LEAVE:
                    return leaveService.findById(id)
                            .map(ResponseEntity::ok)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                                    "Leave not found with ID: " + id));
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported entity type: " + type);
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid entity type: " + type);
        }
    }
    
    @PostMapping("/ticket")
    public ResponseEntity<Ticket> createTicket(@Valid @RequestBody Ticket ticket) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.save(ticket));
    }
    
    @PostMapping("/roster")
    public ResponseEntity<Roster> createRoster(@Valid @RequestBody Roster roster) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rosterService.save(roster));
    }
    
    @PostMapping("/leave")
    public ResponseEntity<Leave> createLeave(@Valid @RequestBody Leave leave) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.save(leave));
    }
    
    @PutMapping("/ticket/{id}")
    public ResponseEntity<Ticket> updateTicket(@PathVariable Long id, @Valid @RequestBody Ticket ticket) {
        if (!ticketService.findById(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found with ID: " + id);
        }
        
        ticket.setId(id);
        return ResponseEntity.ok(ticketService.save(ticket));
    }
    
    @PutMapping("/roster/{id}")
    public ResponseEntity<Roster> updateRoster(@PathVariable Long id, @Valid @RequestBody Roster roster) {
        if (!rosterService.findById(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Roster not found with ID: " + id);
        }
        
        roster.setId(id);
        return ResponseEntity.ok(rosterService.save(roster));
    }
    
    @PutMapping("/leave/{id}")
    public ResponseEntity<Leave> updateLeave(@PathVariable Long id, @Valid @RequestBody Leave leave) {
        if (!leaveService.findById(id).isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave not found with ID: " + id);
        }
        
        leave.setId(id);
        return ResponseEntity.ok(leaveService.save(leave));
    }
    
    @DeleteMapping("/{type}/{id}")
    public ResponseEntity<Void> deleteEntity(@PathVariable String type, @PathVariable Long id) {
        try {
            EntityType entityType = EntityType.valueOf(type.toUpperCase());
            
            switch (entityType) {
                case TICKET:
                    if (!ticketService.findById(id).isPresent()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found with ID: " + id);
                    }
                    ticketService.deleteById(id);
                    break;
                case ROSTER:
                    if (!rosterService.findById(id).isPresent()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Roster not found with ID: " + id);
                    }
                    rosterService.deleteById(id);
                    break;
                case LEAVE:
                    if (!leaveService.findById(id).isPresent()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave not found with ID: " + id);
                    }
                    leaveService.deleteById(id);
                    break;
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported entity type: " + type);
            }
            
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid entity type: " + type);
        }
    }
}
