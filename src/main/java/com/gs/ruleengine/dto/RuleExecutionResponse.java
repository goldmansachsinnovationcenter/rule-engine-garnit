package com.gs.ruleengine.dto;

import com.gs.ruleengine.model.ActionOutput;
import com.gs.ruleengine.model.RuleEngineOutput;
import java.util.List;

public class RuleExecutionResponse {
    private List<RuleEngineOutput> ruleEngineOutputs;
    private List<ActionOutput> actionOutputs;
    
    public RuleExecutionResponse() {}
    
    public RuleExecutionResponse(List<RuleEngineOutput> ruleEngineOutputs, List<ActionOutput> actionOutputs) {
        this.ruleEngineOutputs = ruleEngineOutputs;
        this.actionOutputs = actionOutputs;
    }
    
    // Getters and setters
    public List<RuleEngineOutput> getRuleEngineOutputs() { return ruleEngineOutputs; }
    public void setRuleEngineOutputs(List<RuleEngineOutput> ruleEngineOutputs) { this.ruleEngineOutputs = ruleEngineOutputs; }
    
    public List<ActionOutput> getActionOutputs() { return actionOutputs; }
    public void setActionOutputs(List<ActionOutput> actionOutputs) { this.actionOutputs = actionOutputs; }
}
