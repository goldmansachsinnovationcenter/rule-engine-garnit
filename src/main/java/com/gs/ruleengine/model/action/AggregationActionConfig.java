package com.gs.ruleengine.model.action;

import java.util.List;

/**
 * Configuration for aggregation actions.
 */
public class AggregationActionConfig {
    
    private String aggregationField;
    private String aggregationType; // SUM, AVG, COUNT, MIN, MAX
    private String groupByField;
    private List<String> filterFields;
    private String outputDestination; // DB, FILE, API
    
    public AggregationActionConfig() {
    }
    
    public String getAggregationField() {
        return aggregationField;
    }
    
    public void setAggregationField(String aggregationField) {
        this.aggregationField = aggregationField;
    }
    
    public String getAggregationType() {
        return aggregationType;
    }
    
    public void setAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
    }
    
    public String getGroupByField() {
        return groupByField;
    }
    
    public void setGroupByField(String groupByField) {
        this.groupByField = groupByField;
    }
    
    public List<String> getFilterFields() {
        return filterFields;
    }
    
    public void setFilterFields(List<String> filterFields) {
        this.filterFields = filterFields;
    }
    
    public String getOutputDestination() {
        return outputDestination;
    }
    
    public void setOutputDestination(String outputDestination) {
        this.outputDestination = outputDestination;
    }
}
