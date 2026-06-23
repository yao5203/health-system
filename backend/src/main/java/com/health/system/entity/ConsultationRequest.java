package com.health.system.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "consultation_request")
public class ConsultationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "doctor_id")
    private Long doctorId;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "issue_type")
    private String issueType;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "preferred_tag")
    private String preferredTag;

    private String status;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "assign_time")
    private LocalDateTime assignTime;

    @Column(name = "close_time")
    private LocalDateTime closeTime;

    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "PENDING_ASSIGNMENT";
        }
    }
}
