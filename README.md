# Rule Engine Application

A Spring Boot application that implements a rule engine with the following features:

## Requirements
- Rule engine that takes a collection of rules as input
- Rule objects contain expression tree, ruleId, name, and entity type (Ticket, Roster, Leave)
- Expression support for AND, OR operators
- Conditions that work on any field of an entity
- Rule engine produces output object with essential details
- Action engine that receives output from rule engine
- Action configurations for each rule
- Action types: email, aggregation of entity, setting property on entity
- Action engine produces action output with essential details

## API Endpoints
All API endpoints are prefixed with '/api' as per requirements.
