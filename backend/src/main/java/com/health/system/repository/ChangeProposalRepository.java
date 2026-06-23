package com.health.system.repository;

import com.health.system.entity.ChangeProposal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeProposalRepository extends JpaRepository<ChangeProposal, Long> {

    List<ChangeProposal> findAllByOrderByCreateTimeDesc();

    List<ChangeProposal> findByStatusOrderByCreateTimeDesc(String status);

    List<ChangeProposal> findByProposerDoctorIdOrderByCreateTimeDesc(Long proposerDoctorId);
}
