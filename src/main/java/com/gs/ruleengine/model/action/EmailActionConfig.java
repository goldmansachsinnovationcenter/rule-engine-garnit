package com.gs.ruleengine.model.action;

import java.util.List;

/**
 * Configuration for email actions.
 */
public class EmailActionConfig {
    
    private List<String> recipients;
    private String subject;
    private String template;
    private boolean includeEntityDetails;
    
    public EmailActionConfig() {
    }
    
    public List<String> getRecipients() {
        return recipients;
    }
    
    public void setRecipients(List<String> recipients) {
        this.recipients = recipients;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getTemplate() {
        return template;
    }
    
    public void setTemplate(String template) {
        this.template = template;
    }
    
    public boolean isIncludeEntityDetails() {
        return includeEntityDetails;
    }
    
    public void setIncludeEntityDetails(boolean includeEntityDetails) {
        this.includeEntityDetails = includeEntityDetails;
    }
}
