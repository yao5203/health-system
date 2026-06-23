package com.health.system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "change_proposal")
public class ChangeProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proposer_doctor_id")
    private Long proposerDoctorId;

    @Column(name = "proposer_name")
    private String proposerName;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "target_id")
    private Long targetId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "payload_json", columnDefinition = "LONGTEXT")
    private String payloadJson;

    private String status;

    @Column(name = "reviewer_admin_id")
    private Long reviewerAdminId;

    @Column(name = "reviewer_name")
    private String reviewerName;

    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "review_time")
    private LocalDateTime reviewTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "PENDING";
        }
    }
}
