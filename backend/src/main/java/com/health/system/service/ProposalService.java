package com.health.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.system.dto.ArticleRequest;
import com.health.system.dto.ProposalReviewRequest;
import com.health.system.dto.ProposalSubmitRequest;
import com.health.system.dto.QuestionRequest;
import com.health.system.entity.ChangeProposal;
import com.health.system.entity.HealthPlan;
import com.health.system.repository.ChangeProposalRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProposalService {

    @Autowired
    private ChangeProposalRepository changeProposalRepository;

    @Autowired
    private DoctorAdminService doctorAdminService;

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private ObjectMapper objectMapper;

    public ChangeProposal submit(ProposalSubmitRequest request) {
        if (request.getProposerDoctorId() == null) {
            throw new RuntimeException("医生ID不能为空");
        }
        if (request.getTargetType() == null || request.getTargetType().isBlank()) {
            throw new RuntimeException("提案类型不能为空");
        }
        if (request.getActionType() == null || request.getActionType().isBlank()) {
            throw new RuntimeException("提案动作不能为空");
        }
        ChangeProposal proposal = new ChangeProposal();
        proposal.setProposerDoctorId(request.getProposerDoctorId());
        proposal.setProposerName(request.getProposerName());
        proposal.setTargetType(request.getTargetType().trim().toUpperCase());
        proposal.setActionType(request.getActionType().trim().toUpperCase());
        proposal.setTargetId(request.getTargetId());
        proposal.setTitle(request.getTitle());
        proposal.setSummary(request.getSummary());
        proposal.setPayloadJson(request.getPayloadJson());
        ChangeProposal saved = changeProposalRepository.save(proposal);
        systemLogService.saveLog("医生提交修改提案:" + saved.getTargetType(), saved.getProposerName());
        return saved;
    }

    public List<ChangeProposal> listAll(String status) {
        if (status == null || status.isBlank()) {
            return changeProposalRepository.findAllByOrderByCreateTimeDesc();
        }
        return changeProposalRepository.findByStatusOrderByCreateTimeDesc(status.toUpperCase());
    }

    public List<ChangeProposal> listByDoctor(Long doctorId) {
        return changeProposalRepository.findByProposerDoctorIdOrderByCreateTimeDesc(doctorId);
    }

    @Transactional
    public ChangeProposal review(Long proposalId, ProposalReviewRequest request) {
        ChangeProposal proposal = changeProposalRepository.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("提案不存在"));
        if (!"PENDING".equalsIgnoreCase(proposal.getStatus())) {
            throw new RuntimeException("该提案已处理，不能重复审核");
        }

        String status = request.getStatus() == null ? "" : request.getStatus().trim().toUpperCase();
        if (!"APPROVED".equals(status) && !"REJECTED".equals(status)) {
            throw new RuntimeException("审核状态仅支持 APPROVED 或 REJECTED");
        }

        if ("APPROVED".equals(status)) {
            applyProposal(proposal, request.getReviewerName());
        }

        proposal.setStatus(status);
        proposal.setReviewerAdminId(request.getReviewerAdminId());
        proposal.setReviewerName(request.getReviewerName());
        proposal.setReviewComment(request.getReviewComment());
        proposal.setReviewTime(LocalDateTime.now());
        ChangeProposal saved = changeProposalRepository.save(proposal);
        systemLogService.saveLog("系统管理员审核提案:" + saved.getId() + ":" + status, request.getReviewerName());
        return saved;
    }

    private void applyProposal(ChangeProposal proposal, String operator) {
        String targetType = proposal.getTargetType();
        String actionType = proposal.getActionType();
        Long targetId = proposal.getTargetId();

        try {
            switch (targetType) {
                case "PLAN" -> applyPlanProposal(actionType, targetId, proposal.getPayloadJson(), operator);
                case "QUESTION" -> applyQuestionProposal(actionType, targetId, proposal.getPayloadJson(), operator);
                case "ARTICLE" -> applyArticleProposal(actionType, targetId, proposal.getPayloadJson(), operator);
                default -> throw new RuntimeException("暂不支持的提案类型:" + targetType);
            }
        } catch (Exception ex) {
            throw new RuntimeException("提案执行失败: " + ex.getMessage(), ex);
        }
    }

    private void applyPlanProposal(String actionType, Long targetId, String payloadJson, String operator) throws Exception {
        if ("DELETE".equals(actionType)) {
            if (targetId == null) {
                throw new RuntimeException("删除方案提案必须提供 targetId");
            }
            doctorAdminService.deletePlan(targetId, operator);
            return;
        }
        HealthPlan payload = objectMapper.readValue(payloadJson, HealthPlan.class);
        if ("CREATE".equals(actionType)) {
            doctorAdminService.createPlan(payload, operator);
            return;
        }
        if ("UPDATE".equals(actionType)) {
            if (targetId == null) {
                throw new RuntimeException("更新方案提案必须提供 targetId");
            }
            doctorAdminService.updatePlan(targetId, payload, operator);
            return;
        }
        throw new RuntimeException("不支持的方案提案动作:" + actionType);
    }

    private void applyQuestionProposal(String actionType, Long targetId, String payloadJson, String operator) throws Exception {
        if ("DELETE".equals(actionType)) {
            if (targetId == null) {
                throw new RuntimeException("删除题目提案必须提供 targetId");
            }
            doctorAdminService.deleteQuestion(targetId, operator);
            return;
        }
        QuestionRequest payload = objectMapper.readValue(payloadJson, QuestionRequest.class);
        if ("CREATE".equals(actionType)) {
            doctorAdminService.createQuestion(payload, operator);
            return;
        }
        if ("UPDATE".equals(actionType)) {
            if (targetId == null) {
                throw new RuntimeException("更新题目提案必须提供 targetId");
            }
            doctorAdminService.updateQuestion(targetId, payload, operator);
            return;
        }
        throw new RuntimeException("不支持的题目提案动作:" + actionType);
    }

    private void applyArticleProposal(String actionType, Long targetId, String payloadJson, String operator) throws Exception {
        if ("DELETE".equals(actionType)) {
            if (targetId == null) {
                throw new RuntimeException("删除资讯提案必须提供 targetId");
            }
            doctorAdminService.deleteArticle(targetId, operator);
            return;
        }
        ArticleRequest payload = objectMapper.readValue(payloadJson, ArticleRequest.class);
        if ("CREATE".equals(actionType)) {
            doctorAdminService.createArticle(payload, operator);
            return;
        }
        if ("UPDATE".equals(actionType)) {
            if (targetId == null) {
                throw new RuntimeException("更新资讯提案必须提供 targetId");
            }
            doctorAdminService.updateArticle(targetId, payload, operator);
            return;
        }
        throw new RuntimeException("不支持的资讯提案动作:" + actionType);
    }
}
