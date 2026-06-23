package com.health.system.service;

import com.health.system.entity.HealthData;
import com.health.system.entity.HealthResult;
import com.health.system.repository.HealthDataRepository;
import com.health.system.repository.HealthResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HealthDataService {

    @Autowired
    private HealthDataRepository repository;

    @Autowired
    private HealthResultRepository healthResultRepository;

    @Autowired
    private EvaluationService evaluationService;

    /**
     * 新增健康数据 + 自动评估 + 推荐康养方案
     */
    public Map<String, Object> add(HealthData data) {
        validateHealthData(data);

        Map<String, Object> snapshot = evaluationService.buildAssessmentSnapshot(data, data.getUserId(), null);
        int score = (Integer) snapshot.get("score");
        String level = (String) snapshot.get("healthLevel");
        String healthType = (String) snapshot.get("constitution");
        String description = (String) snapshot.get("constitutionDescription");

        repository.save(data);

        HealthResult result = new HealthResult();
        result.setUserId(data.getUserId());
        result.setScore((double) score);
        result.setHealthLevel(level);
        result.setConstitutionType(healthType);
        healthResultRepository.save(result);

        Map<String, Object> response = new HashMap<>();
        response.put("phase", "initial");
        response.putAll(snapshot);
        response.put("score", score);
        response.put("healthLevel", level);
        response.put("initialConstitution", healthType);
        response.put("constitutionDescription", description);
        response.put("message", "健康硬性指标初评完成，请继续进行动态问卷评测。");
        return response;
    }

    private void validateHealthData(HealthData data) {
        if (data == null || data.getUserId() == null) {
            throw new RuntimeException("用户信息不能为空");
        }
        boolean hasBloodPressure = data.getBloodPressureHigh() != null && data.getBloodPressureLow() != null;
        boolean hasBloodSugar = data.getBloodSugar() != null;
        boolean hasHeartRate = data.getHeartRate() != null;
        boolean hasBloodOxygen = data.getBloodOxygen() != null;
        boolean hasBodyShape = data.getHeight() != null && data.getWeight() != null && data.getHeight() > 0;

        if (!hasBloodPressure && !hasBloodSugar && !hasHeartRate && !hasBloodOxygen && !hasBodyShape) {
            throw new RuntimeException("请至少填写一项可分析的硬性健康指标。血压需高压和低压同时填写，身高和体重需同时填写。");
        }
    }
}
