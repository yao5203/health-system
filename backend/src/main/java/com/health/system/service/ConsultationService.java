package com.health.system.service;

import com.health.system.dto.ConsultationApplyRequest;
import com.health.system.dto.ConsultationAssignRequest;
import com.health.system.dto.ConsultationMessageRequest;
import com.health.system.entity.ConsultationMessage;
import com.health.system.entity.ConsultationRequest;
import com.health.system.entity.DailyCheckin;
import com.health.system.entity.Doctor;
import com.health.system.entity.HealthData;
import com.health.system.entity.HealthResult;
import com.health.system.entity.User;
import com.health.system.repository.ConsultationMessageRepository;
import com.health.system.repository.ConsultationRequestRepository;
import com.health.system.repository.DailyCheckinRepository;
import com.health.system.repository.DoctorRepository;
import com.health.system.repository.HealthDataRepository;
import com.health.system.repository.HealthResultRepository;
import com.health.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConsultationService {

    @Autowired
    private ConsultationRequestRepository consultationRequestRepository;

    @Autowired
    private ConsultationMessageRepository consultationMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private HealthDataRepository healthDataRepository;

    @Autowired
    private HealthResultRepository healthResultRepository;

    @Autowired
    private DailyCheckinRepository dailyCheckinRepository;

    @Autowired
    private SystemLogService systemLogService;

    public ConsultationRequest apply(ConsultationApplyRequest request) {
        if (request.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("咨询标题不能为空");
        }
        ConsultationRequest consultation = new ConsultationRequest();
        consultation.setUserId(request.getUserId());
        consultation.setIssueType(request.getIssueType());
        consultation.setTitle(request.getTitle());
        consultation.setDetail(request.getDetail());
        consultation.setPreferredTag(request.getPreferredTag());
        ConsultationRequest saved = consultationRequestRepository.save(consultation);

        if (request.getDetail() != null && !request.getDetail().isBlank()) {
            User user = userRepository.findById(request.getUserId()).orElse(null);
            ConsultationMessage message = new ConsultationMessage();
            message.setConsultationId(saved.getId());
            message.setSenderType("USER");
            message.setSenderId(request.getUserId());
            message.setSenderName(user == null ? "用户" : user.getUsername());
            message.setContent(request.getDetail().trim());
            consultationMessageRepository.save(message);
        }

        systemLogService.saveLog("用户提交医生咨询申请", "user:" + request.getUserId());
        return saved;
    }

    public List<Map<String, Object>> listAll() {
        return consultationRequestRepository.findAllByOrderByCreateTimeDesc()
                .stream()
                .map(this::buildConsultationView)
                .toList();
    }

    public List<Map<String, Object>> listByUser(Long userId) {
        return consultationRequestRepository.findByUserIdOrderByCreateTimeDesc(userId)
                .stream()
                .map(this::buildConsultationView)
                .toList();
    }

    public List<Map<String, Object>> listByDoctor(Long doctorId) {
        return consultationRequestRepository.findByDoctorIdOrderByCreateTimeDesc(doctorId)
                .stream()
                .map(this::buildConsultationView)
                .toList();
    }

    public ConsultationRequest assign(Long consultationId, ConsultationAssignRequest request) {
        ConsultationRequest consultation = consultationRequestRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("咨询申请不存在"));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("匹配医生不存在"));

        consultation.setDoctorId(doctor.getId());
        consultation.setAdminId(request.getAdminId());
        consultation.setAdminNote(request.getAdminNote());
        consultation.setStatus("ASSIGNED");
        consultation.setAssignTime(LocalDateTime.now());
        ConsultationRequest saved = consultationRequestRepository.save(consultation);
        systemLogService.saveLog("系统管理员分配医生咨询:" + consultationId, request.getAdminName());
        return saved;
    }

    public ConsultationRequest close(Long consultationId, String operator) {
        ConsultationRequest consultation = consultationRequestRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("咨询申请不存在"));
        consultation.setStatus("CLOSED");
        consultation.setCloseTime(LocalDateTime.now());
        ConsultationRequest saved = consultationRequestRepository.save(consultation);
        systemLogService.saveLog("关闭咨询会话:" + consultationId, operator);
        return saved;
    }

    public ConsultationMessage sendMessage(Long consultationId, ConsultationMessageRequest request) {
        ConsultationRequest consultation = consultationRequestRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("咨询申请不存在"));
        if ("PENDING_ASSIGNMENT".equals(consultation.getStatus()) && !"USER".equalsIgnoreCase(request.getSenderType())) {
            throw new RuntimeException("咨询尚未分配医生，当前仅支持用户补充说明");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new RuntimeException("消息内容不能为空");
        }
        ConsultationMessage message = new ConsultationMessage();
        message.setConsultationId(consultationId);
        message.setSenderType(request.getSenderType());
        message.setSenderId(request.getSenderId());
        message.setSenderName(request.getSenderName());
        message.setContent(request.getContent().trim());
        ConsultationMessage saved = consultationMessageRepository.save(message);
        systemLogService.saveLog("发送咨询消息:" + consultationId, request.getSenderName());
        return saved;
    }

    public List<ConsultationMessage> listMessages(Long consultationId) {
        return consultationMessageRepository.findByConsultationIdOrderByCreateTimeAsc(consultationId);
    }

    public Map<String, Object> getDoctorConsultationSnapshot(Long consultationId, Long doctorId) {
        ConsultationRequest consultation = consultationRequestRepository.findById(consultationId)
                .orElseThrow(() -> new RuntimeException("咨询申请不存在"));
        if (consultation.getDoctorId() == null || !consultation.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("你只能查看分配给自己的咨询档案");
        }

        User user = userRepository.findById(consultation.getUserId())
                .orElseThrow(() -> new RuntimeException("关联用户不存在"));
        HealthData latestHealthData = healthDataRepository.findTopByUserIdOrderByIdDesc(user.getId());
        HealthResult latestHealthResult = healthResultRepository.findTopByUserIdOrderByCreateTimeDesc(user.getId());
        List<DailyCheckin> allCheckins = dailyCheckinRepository.findByUserIdOrderByCheckinDateDescCreateTimeDesc(user.getId());

        List<Map<String, Object>> recentCheckins = new ArrayList<>();
        allCheckins.stream().limit(7).forEach(item -> recentCheckins.add(buildCheckinView(item)));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userProfile", buildUserProfile(user));
        result.put("latestHealthData", latestHealthData);
        result.put("latestHealthResult", latestHealthResult);
        result.put("recentCheckins", recentCheckins);
        result.put("healthSummary", buildHealthSummary(latestHealthData, latestHealthResult, recentCheckins));
        result.put("consultationMeta", buildConsultationMeta(consultation));
        return result;
    }

    public List<Map<String, Object>> listRecommendedDoctors(String preferredTag) {
        String tag = preferredTag == null ? "" : preferredTag.trim();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Doctor doctor : doctorRepository.findAll()) {
            if (doctor.getStatus() != null && doctor.getStatus() == 0) {
                continue;
            }
            boolean matched = tag.isBlank()
                    || containsIgnoreCase(doctor.getSpecialty(), tag)
                    || containsIgnoreCase(doctor.getExpertiseTags(), tag);
            if (!matched) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("doctorId", doctor.getId());
            item.put("username", doctor.getUsername());
            item.put("specialty", doctor.getSpecialty());
            item.put("expertiseTags", doctor.getExpertiseTags());
            item.put("introduction", doctor.getIntroduction());
            result.add(item);
        }
        return result;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        return source != null && source.toLowerCase().contains(keyword.toLowerCase());
    }

    private Map<String, Object> buildConsultationView(ConsultationRequest consultation) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", consultation.getId());
        item.put("userId", consultation.getUserId());
        item.put("doctorId", consultation.getDoctorId());
        item.put("adminId", consultation.getAdminId());
        item.put("issueType", consultation.getIssueType());
        item.put("title", consultation.getTitle());
        item.put("detail", consultation.getDetail());
        item.put("preferredTag", consultation.getPreferredTag());
        item.put("status", consultation.getStatus());
        item.put("adminNote", consultation.getAdminNote());
        item.put("createTime", consultation.getCreateTime());
        item.put("assignTime", consultation.getAssignTime());
        item.put("closeTime", consultation.getCloseTime());
        item.put("messages", consultationMessageRepository.findByConsultationIdOrderByCreateTimeAsc(consultation.getId()));
        userRepository.findById(consultation.getUserId()).ifPresent(user -> item.put("username", user.getUsername()));
        if (consultation.getDoctorId() != null) {
            doctorRepository.findById(consultation.getDoctorId()).ifPresent(doctor -> {
                item.put("doctorName", doctor.getUsername());
                item.put("doctorSpecialty", doctor.getSpecialty());
            });
        }
        return item;
    }

    private Map<String, Object> buildUserProfile(User user) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("age", user.getAge());
        profile.put("gender", user.getGender());
        profile.put("phone", user.getPhone());
        profile.put("createTime", user.getCreateTime());
        return profile;
    }

    private Map<String, Object> buildCheckinView(DailyCheckin checkin) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", checkin.getId());
        item.put("checkinDate", checkin.getCheckinDate());
        item.put("planTitle", checkin.getPlanTitle());
        item.put("sleepHours", checkin.getSleepHours());
        item.put("exerciseMinutes", checkin.getExerciseMinutes());
        item.put("stressLevel", checkin.getStressLevel());
        item.put("moodScore", checkin.getMoodScore());
        item.put("weight", checkin.getWeight());
        item.put("bloodPressureHigh", checkin.getBloodPressureHigh());
        item.put("bloodPressureLow", checkin.getBloodPressureLow());
        item.put("remark", checkin.getRemark());
        item.put("createTime", checkin.getCreateTime());
        return item;
    }

    private Map<String, Object> buildHealthSummary(HealthData latestHealthData,
                                                   HealthResult latestHealthResult,
                                                   List<Map<String, Object>> recentCheckins) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("checkinCount", recentCheckins.size());
        summary.put("latestScore", latestHealthResult == null ? null : latestHealthResult.getScore());
        summary.put("latestHealthLevel", latestHealthResult == null ? null : latestHealthResult.getHealthLevel());
        summary.put("latestConstitutionType", latestHealthResult == null ? null : latestHealthResult.getConstitutionType());
        summary.put("latestWeight", latestHealthData == null ? null : latestHealthData.getWeight());
        summary.put("latestBloodSugar", latestHealthData == null ? null : latestHealthData.getBloodSugar());
        summary.put("latestHeartRate", latestHealthData == null ? null : latestHealthData.getHeartRate());
        summary.put("latestBloodOxygen", latestHealthData == null ? null : latestHealthData.getBloodOxygen());
        summary.put("latestBloodPressure", latestHealthData == null
                ? null
                : buildBloodPressureText(latestHealthData.getBloodPressureHigh(), latestHealthData.getBloodPressureLow()));
        summary.put("averageSleepHours", averageNumber(recentCheckins, "sleepHours"));
        summary.put("averageExerciseMinutes", averageNumber(recentCheckins, "exerciseMinutes"));
        summary.put("averageStressLevel", averageNumber(recentCheckins, "stressLevel"));
        summary.put("averageMoodScore", averageNumber(recentCheckins, "moodScore"));
        return summary;
    }

    private Map<String, Object> buildConsultationMeta(ConsultationRequest consultation) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("status", consultation.getStatus());
        meta.put("issueType", consultation.getIssueType());
        meta.put("preferredTag", consultation.getPreferredTag());
        meta.put("adminNote", consultation.getAdminNote());
        meta.put("createTime", consultation.getCreateTime());
        meta.put("assignTime", consultation.getAssignTime());
        meta.put("closeTime", consultation.getCloseTime());
        return meta;
    }

    private String buildBloodPressureText(Integer high, Integer low) {
        if (high == null && low == null) {
            return null;
        }
        return String.format("%s / %s", high == null ? "-" : high, low == null ? "-" : low);
    }

    private Double averageNumber(List<Map<String, Object>> rows, String key) {
        double total = 0;
        int count = 0;
        for (Map<String, Object> row : rows) {
            Object value = row.get(key);
            if (value instanceof Number number) {
                total += number.doubleValue();
                count++;
            }
        }
        if (count == 0) {
            return null;
        }
        return Math.round(total * 10.0 / count) / 10.0;
    }
}
