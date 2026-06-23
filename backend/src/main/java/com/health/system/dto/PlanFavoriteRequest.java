package com.health.system.dto;

import lombok.Data;

@Data
public class PlanFavoriteRequest {
    private Long userId;
    private Long planId;
    private String planTitle;
    private String constitutionType;
    private String healthLevel;
    private String diet;
    private String drink;
    private String sport;
    private String lifestyle;
}
