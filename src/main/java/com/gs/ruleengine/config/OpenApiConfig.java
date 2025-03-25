package com.gs.ruleengine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI ruleEngineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rule Engine API")
                        .description("API for rule engine application that processes rules and executes actions")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Goldman Sachs Innovation Center")
                                .url("https://www.goldmansachs.com")
                                .email("innovation@gs.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://www.goldmansachs.com")));
    }
}
