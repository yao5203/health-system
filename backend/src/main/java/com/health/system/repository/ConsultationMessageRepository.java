package com.health.system.repository;

import com.health.system.entity.ConsultationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultationMessageRepository extends JpaRepository<ConsultationMessage, Long> {

    List<ConsultationMessage> findByConsultationIdOrderByCreateTimeAsc(Long consultationId);
}
