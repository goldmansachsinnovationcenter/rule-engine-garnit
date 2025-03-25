package com.gs.ruleengine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gs.ruleengine.model.expression.AndExpression;
import com.gs.ruleengine.model.expression.Condition;
import com.gs.ruleengine.model.expression.Expression;
import com.gs.ruleengine.model.expression.OrExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Register subtypes for polymorphic deserialization
        objectMapper.registerSubtypes(
            new NamedType(AndExpression.class, "AND"),
            new NamedType(OrExpression.class, "OR"),
            new NamedType(Condition.class, "CONDITION")
        );
        
        // Configure serialization features
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        
        return objectMapper;
    }
}
