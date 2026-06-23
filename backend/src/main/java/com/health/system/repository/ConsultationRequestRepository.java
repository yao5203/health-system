package com.health.system.repository;

import com.health.system.entity.ConsultationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, Long> {

    List<ConsultationRequest> findAllByOrderByCreateTimeDesc();

    List<ConsultationRequest> findByUserIdOrderByCreateTimeDesc(Long userId);

    List<ConsultationRequest> findByDoctorIdOrderByCreateTimeDesc(Long doctorId);

    List<ConsultationRequest> findByStatusOrderByCreateTimeDesc(String status);
}
