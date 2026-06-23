package com.health.system.service;

import com.health.system.dto.DoctorLoginRequest;
import com.health.system.dto.DoctorProfileRequest;
import com.health.system.dto.DoctorRegisterRequest;
import com.health.system.entity.ChangeProposal;
import com.health.system.entity.ConsultationRequest;
import com.health.system.entity.Doctor;
import com.health.system.repository.ChangeProposalRepository;
import com.health.system.repository.ConsultationRequestRepository;
import com.health.system.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private ConsultationRequestRepository consultationRequestRepository;

    @Autowired
    private ChangeProposalRepository changeProposalRepository;

    public Map<String, Object> register(DoctorRegisterRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            result.put("success", false);
            result.put("message", "医生用户名不能为空");
            return result;
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            result.put("success", false);
            result.put("message", "医生密码不能为空");
            return result;
        }

        Doctor existDoctor = doctorRepository.findByUsername(request.getUsername());
        if (existDoctor != null) {
            result.put("success", false);
            result.put("message", "医生账号已存在");
            return result;
        }

        Doctor doctor = new Doctor();
        doctor.setUsername(request.getUsername());
        doctor.setPassword(request.getPassword());
        doctor.setSpecialty(request.getSpecialty());
        doctor.setExpertiseTags(request.getExpertiseTags());
        doctor.setIntroduction(request.getIntroduction());
        Doctor savedDoctor = doctorRepository.save(doctor);

        systemLogService.saveLog("医生注册", savedDoctor.getUsername());

        result.put("success", true);
        result.put("message", "注册成功");
        result.put("doctorId", savedDoctor.getId());
        result.put("username", savedDoctor.getUsername());
        result.put("specialty", savedDoctor.getSpecialty());
        result.put("expertiseTags", savedDoctor.getExpertiseTags());
        result.put("introduction", savedDoctor.getIntroduction());
        return result;
    }

    public Map<String, Object> login(DoctorLoginRequest request) {
        Doctor doctor = doctorRepository.findByUsername(request.getUsername());
        Map<String, Object> result = new HashMap<>();

        if (doctor == null) {
            result.put("success", false);
            result.put("message", "医生账号不存在");
            return result;
        }

        if (!doctor.getPassword().equals(request.getPassword())) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }

        systemLogService.saveLog("医生登录", doctor.getUsername());

        result.put("success", true);
        result.put("message", "登录成功");
        result.put("doctorId", doctor.getId());
        result.put("username", doctor.getUsername());
        result.put("specialty", doctor.getSpecialty());
        result.put("expertiseTags", doctor.getExpertiseTags());
        result.put("introduction", doctor.getIntroduction());
        return result;
    }

    public Map<String, Object> updateProfile(Long doctorId, DoctorProfileRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        doctor.setSpecialty(request.getSpecialty());
        doctor.setExpertiseTags(request.getExpertiseTags());
        doctor.setIntroduction(request.getIntroduction());
        if (request.getStatus() != null) {
            doctor.setStatus(request.getStatus());
        }
        Doctor saved = doctorRepository.save(doctor);
        systemLogService.saveLog("医生更新个人资料", saved.getUsername());
        return buildDoctorMap(saved);
    }

    public List<Map<String, Object>> listDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::buildDoctorMap)
                .toList();
    }

    public Map<String, Object> getDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        return buildDoctorMap(doctor);
    }

    public Map<String, Object> getWorkspaceOverview(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("医生不存在"));

        List<ConsultationRequest> consultations = consultationRequestRepository.findByDoctorIdOrderByCreateTimeDesc(doctorId);
        List<ChangeProposal> proposals = changeProposalRepository.findByProposerDoctorIdOrderByCreateTimeDesc(doctorId);

        long activeConsultationCount = consultations.stream()
                .filter(item -> !"CLOSED".equalsIgnoreCase(item.getStatus()))
                .count();
        long closedConsultationCount = consultations.stream()
                .filter(item -> "CLOSED".equalsIgnoreCase(item.getStatus()))
                .count();
        long pendingProposalCount = proposals.stream()
                .filter(item -> "PENDING".equalsIgnoreCase(item.getStatus()))
                .count();
        long approvedProposalCount = proposals.stream()
                .filter(item -> "APPROVED".equalsIgnoreCase(item.getStatus()))
                .count();
        long rejectedProposalCount = proposals.stream()
                .filter(item -> "REJECTED".equalsIgnoreCase(item.getStatus()))
                .count();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("doctor", buildDoctorMap(doctor));
        result.put("stats", buildOverviewStats(
                consultations.size(),
                activeConsultationCount,
                closedConsultationCount,
                proposals.size(),
                pendingProposalCount,
                approvedProposalCount,
                rejectedProposalCount,
                calculateProfileCompleteness(doctor)
        ));
        result.put("recentConsultations", buildRecentConsultations(consultations));
        result.put("recentProposals", buildRecentProposals(proposals));
        result.put("focus", buildFocusSuggestions(doctor, activeConsultationCount, pendingProposalCount));
        return result;
    }

    private Map<String, Object> buildDoctorMap(Doctor doctor) {
        Map<String, Object> map = new HashMap<>();
        map.put("doctorId", doctor.getId());
        map.put("id", doctor.getId());
        map.put("username", doctor.getUsername());
        map.put("specialty", doctor.getSpecialty());
        map.put("expertiseTags", doctor.getExpertiseTags());
        map.put("introduction", doctor.getIntroduction());
        map.put("status", doctor.getStatus());
        map.put("createTime", doctor.getCreateTime());
        return map;
    }

    private Map<String, Object> buildOverviewStats(long consultationCount,
                                                   long activeConsultationCount,
                                                   long closedConsultationCount,
                                                   long proposalCount,
                                                   long pendingProposalCount,
                                                   long approvedProposalCount,
                                                   long rejectedProposalCount,
                                                   int profileCompleteness) {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("consultationCount", consultationCount);
        stats.put("activeConsultationCount", activeConsultationCount);
        stats.put("closedConsultationCount", closedConsultationCount);
        stats.put("proposalCount", proposalCount);
        stats.put("pendingProposalCount", pendingProposalCount);
        stats.put("approvedProposalCount", approvedProposalCount);
        stats.put("rejectedProposalCount", rejectedProposalCount);
        stats.put("profileCompleteness", profileCompleteness);
        return stats;
    }

    private List<Map<String, Object>> buildRecentConsultations(List<ConsultationRequest> consultations) {
        List<Map<String, Object>> result = new ArrayList<>();
        consultations.stream().limit(5).forEach(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("title", item.getTitle());
            row.put("issueType", item.getIssueType());
            row.put("status", item.getStatus());
            row.put("createTime", item.getCreateTime());
            row.put("assignTime", item.getAssignTime());
            row.put("closeTime", item.getCloseTime());
            result.add(row);
        });
        return result;
    }

    private List<Map<String, Object>> buildRecentProposals(List<ChangeProposal> proposals) {
        List<Map<String, Object>> result = new ArrayList<>();
        proposals.stream().limit(5).forEach(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("title", item.getTitle());
            row.put("targetType", item.getTargetType());
            row.put("actionType", item.getActionType());
            row.put("status", item.getStatus());
            row.put("reviewComment", item.getReviewComment());
            row.put("createTime", item.getCreateTime());
            result.add(row);
        });
        return result;
    }

    private List<String> buildFocusSuggestions(Doctor doctor, long activeConsultationCount, long pendingProposalCount) {
        List<String> suggestions = new ArrayList<>();
        if (activeConsultationCount > 0) {
            suggestions.add("优先处理仍在进行中的咨询对话，避免用户长时间等待。");
        } else {
            suggestions.add("当前没有进行中的咨询，可以把时间放在完善专业资料和复盘提案上。");
        }

        if (pendingProposalCount > 0) {
            suggestions.add("你有待审核提案，建议补充标题、摘要和适用人群说明，提高通过率。");
        } else {
            suggestions.add("可以继续从题库、方案和资讯里沉淀新的优化提案。");
        }

        if (calculateProfileCompleteness(doctor) < 100) {
            suggestions.add("先补全专业方向、擅长标签和个人简介，便于管理员更准确地分配咨询。");
        } else {
            suggestions.add("当前资料较完整，管理员可以更容易按你的专长匹配合适咨询。");
        }
        return suggestions;
    }

    private int calculateProfileCompleteness(Doctor doctor) {
        int total = 3;
        int filled = 0;
        if (doctor.getSpecialty() != null && !doctor.getSpecialty().isBlank()) {
            filled++;
        }
        if (doctor.getExpertiseTags() != null && !doctor.getExpertiseTags().isBlank()) {
            filled++;
        }
        if (doctor.getIntroduction() != null && !doctor.getIntroduction().isBlank()) {
            filled++;
        }
        return (int) Math.round(filled * 100.0 / total);
    }
}
