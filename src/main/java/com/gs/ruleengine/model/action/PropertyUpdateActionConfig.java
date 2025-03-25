package com.gs.ruleengine.model.action;

import java.util.Map;

/**
 * Configuration for property update actions.
 */
public class PropertyUpdateActionConfig {
    
    private Map<String, Object> propertiesToUpdate;
    
    public PropertyUpdateActionConfig() {
    }
    
    public Map<String, Object> getPropertiesToUpdate() {
        return propertiesToUpdate;
    }
    
    public void setPropertiesToUpdate(Map<String, Object> propertiesToUpdate) {
        this.propertiesToUpdate = propertiesToUpdate;
    }
}
