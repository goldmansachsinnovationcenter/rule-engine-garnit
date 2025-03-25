package com.gs.ruleengine.engine;

import com.gs.ruleengine.model.Leave;
import com.gs.ruleengine.model.Roster;
import com.gs.ruleengine.model.Ticket;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * Utility for extracting entity data as a map of field names to values.
 */
@Component
public class EntityDataExtractor {
    
    private static final Logger logger = LoggerFactory.getLogger(EntityDataExtractor.class);
    
    /**
     * Extracts data from a Ticket entity as a map.
     * 
     * @param ticket The ticket entity
     * @return Map of field names to values
     */
    public Map<String, Object> extractData(Ticket ticket) {
        return extractEntityData(ticket);
    }
    
    /**
     * Extracts data from a Roster entity as a map.
     * 
     * @param roster The roster entity
     * @return Map of field names to values
     */
    public Map<String, Object> extractData(Roster roster) {
        return extractEntityData(roster);
    }
    
    /**
     * Extracts data from a Leave entity as a map.
     * 
     * @param leave The leave entity
     * @return Map of field names to values
     */
    public Map<String, Object> extractData(Leave leave) {
        return extractEntityData(leave);
    }
    
    /**
     * Generic method to extract data from any entity using reflection.
     * 
     * @param entity The entity object
     * @return Map of field names to values
     */
    private Map<String, Object> extractEntityData(Object entity) {
        Map<String, Object> data = new HashMap<>();
        
        if (entity == null) {
            return data;
        }
        
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(entity.getClass());
        
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propertyName = propertyDescriptor.getName();
            
            // Skip class property
            if ("class".equals(propertyName)) {
                continue;
            }
            
            Method readMethod = propertyDescriptor.getReadMethod();
            
            if (readMethod != null) {
                try {
                    Object value = readMethod.invoke(entity);
                    data.put(propertyName, value);
                } catch (Exception e) {
                    logger.error("Error extracting property {} from entity {}", propertyName, entity.getClass().getSimpleName(), e);
                }
            }
        }
        
        return data;
    }
}
