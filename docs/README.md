# Rule Engine Application Documentation

## Overview

The Rule Engine Application is a Spring Boot-based system designed to evaluate business rules against different entity types and execute configurable actions based on rule evaluation results. This documentation provides a comprehensive guide to understanding and using the rule engine.

## Table of Contents

1. [Architecture](#architecture)
2. [Rule Structure](#rule-structure)
3. [Expression Tree](#expression-tree)
4. [Rule Engine](#rule-engine)
5. [Action Engine](#action-engine)
6. [Entity Types](#entity-types)
7. [API Reference](#api-reference)
8. [Configuration Examples](#configuration-examples)

## Architecture

The application follows a modular architecture with the following key components:

- **Rule Engine**: Evaluates rules against entity data
- **Action Engine**: Executes actions based on rule evaluation results
- **Expression Evaluator**: Processes complex expression trees
- **Action Handlers**: Specialized components for different action types
- **REST API**: Endpoints for managing rules and triggering evaluations

![Architecture Diagram](architecture.png)

## Rule Structure

A rule in the system consists of the following components:

- **Rule ID**: Unique identifier for the rule
- **Name**: Human-readable name for the rule
- **Entity Type**: The type of entity the rule applies to (Ticket, Roster, Leave)
- **Expression Tree**: A tree structure representing the conditions to evaluate
- **Active Flag**: Indicates whether the rule is currently active

Example Rule JSON:

```json
{
  "id": 1,
  "name": "High Priority Ticket Rule",
  "entityType": "TICKET",
  "expressionJson": "{\"type\":\"AND\",\"expressions\":[{\"type\":\"CONDITION\",\"field\":\"status\",\"operator\":\"EQUALS\",\"value\":\"OPEN\"},{\"type\":\"CONDITION\",\"field\":\"priority\",\"operator\":\"GREATER_THAN\",\"value\":2}]}",
  "active": true
}
```

## Expression Tree

The expression tree supports complex logical operations with the following components:

### Operators

- **AND**: Logical AND operation between multiple expressions
- **OR**: Logical OR operation between multiple expressions

### Conditions

Conditions are the leaf nodes of the expression tree and contain:

- **Field**: The entity field to evaluate
- **Operator**: Comparison operator (EQUALS, NOT_EQUALS, GREATER_THAN, LESS_THAN, etc.)
- **Value**: The value to compare against

### Expression Structure

Expressions can be nested to create complex conditions:

```
AND
├── Condition (status = "OPEN")
└── OR
    ├── Condition (priority > 2)
    └── Condition (assignee = "support-team")
```

## Rule Engine

The Rule Engine is responsible for:

1. Loading rules for a specific entity type
2. Retrieving entity data
3. Evaluating the expression tree against entity data
4. Producing a rule engine output with evaluation results

### Rule Engine Output

The rule engine produces an output object containing:

- **Rule ID**: ID of the evaluated rule
- **Rule Name**: Name of the evaluated rule
- **Entity Type**: Type of entity evaluated
- **Entity ID**: ID of the entity evaluated
- **Result**: Boolean result of the rule evaluation (true/false)

## Action Engine

The Action Engine executes actions based on rule evaluation results. It supports multiple action types and produces detailed output for each action.

### Action Types

The system supports the following action types:

#### 1. Email Action

Sends email notifications with configurable recipients, subject, and template.

Configuration parameters:
- **recipients**: List of email addresses
- **subject**: Email subject
- **template**: Email template content
- **includeEntityDetails**: Whether to include entity details in the email

#### 2. Aggregation Action

Performs data aggregation operations on entity fields.

Configuration parameters:
- **aggregationField**: Field to aggregate
- **aggregationType**: Type of aggregation (SUM, AVG, COUNT, etc.)
- **groupByField**: Field to group by
- **filterFields**: Fields to filter on
- **outputDestination**: Where to store aggregation results

#### 3. Property Update Action

Updates properties on the entity.

Configuration parameters:
- **propertiesToUpdate**: Map of property names to new values

### Action Configuration

Each action is configured with:

- **Action ID**: Unique identifier for the action
- **Rule ID**: ID of the rule this action is associated with
- **Name**: Human-readable name for the action
- **Action Type**: Type of action (EMAIL, AGGREGATION, PROPERTY_UPDATE)
- **Configuration JSON**: JSON string containing action-specific configuration
- **Active Flag**: Indicates whether the action is currently active

Example Action Configuration:

```json
{
  "id": 1,
  "ruleId": 1,
  "name": "Send High Priority Notification",
  "actionType": "EMAIL",
  "configurationJson": "{\"recipients\":[\"support@example.com\"],\"subject\":\"High Priority Ticket Alert\",\"template\":\"A high priority ticket requires attention\",\"includeEntityDetails\":true}",
  "active": true
}
```

### Action Output

The action engine produces an output object for each action containing:

- **Action Configuration ID**: ID of the action configuration
- **Action Name**: Name of the action
- **Action Type**: Type of action executed
- **Rule ID**: ID of the rule that triggered the action
- **Rule Name**: Name of the rule that triggered the action
- **Entity ID**: ID of the entity the action was performed on
- **Entity Type**: Type of entity the action was performed on
- **Success**: Whether the action was successful
- **Message**: Detailed message about the action execution

## Entity Types

The system supports the following entity types:

### 1. Ticket

Represents a support or service ticket with properties like:
- ID
- Title
- Description
- Status (OPEN, IN_PROGRESS, CLOSED)
- Priority (1-5)
- Assignee
- Created Date
- Updated Date

### 2. Roster

Represents a personnel roster with properties like:
- ID
- Name
- Department
- Role
- Shift
- Start Date
- End Date

### 3. Leave

Represents an employee leave request with properties like:
- ID
- Employee ID
- Start Date
- End Date
- Type (VACATION, SICK, PERSONAL)
- Status (PENDING, APPROVED, REJECTED)
- Reason

## API Reference

The application provides the following REST API endpoints:

### Rule Management

- `GET /api/rules`: Get all rules
- `GET /api/rules/{id}`: Get rule by ID
- `POST /api/rules`: Create a new rule
- `PUT /api/rules/{id}`: Update an existing rule
- `DELETE /api/rules/{id}`: Delete a rule

### Action Configuration Management

- `GET /api/actions`: Get all action configurations
- `GET /api/actions/{id}`: Get action configuration by ID
- `GET /api/actions/rule/{ruleId}`: Get action configurations for a rule
- `POST /api/actions`: Create a new action configuration
- `PUT /api/actions/{id}`: Update an existing action configuration
- `DELETE /api/actions/{id}`: Delete an action configuration

### Rule Engine API

- `POST /api/engine/evaluate/rule/{ruleId}/entity/{entityId}`: Evaluate a specific rule against an entity
- `POST /api/engine/evaluate/entityType/{entityType}/entity/{entityId}`: Evaluate all rules for an entity type against an entity
- `POST /api/engine/execute/rule/{ruleId}/entity/{entityId}`: Evaluate a rule and execute actions
- `POST /api/engine/execute/entityType/{entityType}/entity/{entityId}`: Evaluate all rules for an entity type and execute actions

## Configuration Examples

### Example Rule with AND Condition

```json
{
  "name": "Open High Priority Ticket Rule",
  "entityType": "TICKET",
  "expression": {
    "type": "AND",
    "expressions": [
      {
        "type": "CONDITION",
        "field": "status",
        "operator": "EQUALS",
        "value": "OPEN"
      },
      {
        "type": "CONDITION",
        "field": "priority",
        "operator": "GREATER_THAN",
        "value": 3
      }
    ]
  },
  "active": true
}
```

### Example Rule with OR Condition

```json
{
  "name": "Critical Ticket Rule",
  "entityType": "TICKET",
  "expression": {
    "type": "OR",
    "expressions": [
      {
        "type": "CONDITION",
        "field": "priority",
        "operator": "EQUALS",
        "value": 5
      },
      {
        "type": "CONDITION",
        "field": "tags",
        "operator": "CONTAINS",
        "value": "critical"
      }
    ]
  },
  "active": true
}
```

### Example Email Action Configuration

```json
{
  "ruleId": 1,
  "name": "Send Critical Ticket Notification",
  "actionType": "EMAIL",
  "configurationJson": {
    "recipients": ["support@example.com", "manager@example.com"],
    "subject": "Critical Ticket Alert",
    "template": "A critical ticket requires immediate attention: {{ticket.title}}",
    "includeEntityDetails": true
  },
  "active": true
}
```

### Example Property Update Action Configuration

```json
{
  "ruleId": 1,
  "name": "Auto-assign Critical Ticket",
  "actionType": "PROPERTY_UPDATE",
  "configurationJson": {
    "propertiesToUpdate": {
      "assignee": "critical-response-team",
      "status": "IN_PROGRESS",
      "tags": ["auto-assigned", "critical"]
    }
  },
  "active": true
}
```

### Example Aggregation Action Configuration

```json
{
  "ruleId": 2,
  "name": "Aggregate Open Tickets by Department",
  "actionType": "AGGREGATION",
  "configurationJson": {
    "aggregationField": "count",
    "aggregationType": "COUNT",
    "groupByField": "department",
    "filterFields": ["status"],
    "outputDestination": "DB"
  },
  "active": true
}
```
