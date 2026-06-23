package com.health.system.dto;

import lombok.Data;

@Data
public class ProposalReviewRequest {
    private Long reviewerAdminId;
    private String reviewerName;
    private String status;
    private String reviewComment;
}
