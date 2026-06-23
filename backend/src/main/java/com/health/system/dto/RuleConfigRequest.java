package com.health.system.dto;

import lombok.Data;

@Data
public class RuleConfigRequest {
    private String ruleKey;
    private String ruleName;
    private String ruleValue;
    private String description;
}
