package com.health.system.service;

import com.health.system.dto.QuestionnaireSubmitRequest;
import com.health.system.entity.HealthData;
import com.health.system.entity.HealthPlan;
import com.health.system.entity.HealthPlanTemplate;
import com.health.system.entity.HealthResult;
import com.health.system.entity.Question;
import com.health.system.entity.QuestionOption;
import com.health.system.entity.QuestionnaireQuality;
import com.health.system.entity.UserAnswer;
import com.health.system.repository.HealthDataRepository;
import com.health.system.repository.HealthPlanRepository;
import com.health.system.repository.HealthPlanTemplateRepository;
import com.health.system.repository.HealthResultRepository;
import com.health.system.repository.QuestionOptionRepository;
import com.health.system.repository.QuestionRepository;
import com.health.system.repository.QuestionnaireQualityRepository;
import com.health.system.repository.UserAnswerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class QuestionnaireService {

    private static final int TARGET_QUESTION_COUNT = 30;
    private static final int TOTAL_HARD_INDICATOR_FIELDS = 7;

    private static final List<String> DIMENSIONS = List.of(
            "身体健康状况",
            "生活习惯评估",
            "心理健康状况",
            "睡眠质量评估"
    );

    @Autowired
    private UserAnswerRepository answerRepository;

    @Autowired
    private QuestionOptionRepository optionRepository;

    @Autowired
    private HealthDataRepository healthDataRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private HealthResultRepository healthResultRepository;

    @Autowired
    private HealthPlanRepository healthPlanRepository;

    @Autowired
    private HealthPlanTemplateRepository healthPlanTemplateRepository;

    @Autowired
    private QuestionnaireQualityRepository questionnaireQualityRepository;

    @Autowired
    private EvaluationService evaluationService;

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public Map<String, Object> getRecommendedQuestions(Long userId) {
        Map<String, Object> result = new LinkedHashMap<>();

        HealthData data = healthDataRepository.findTopByUserIdOrderByIdDesc(userId);
        String healthType = null;
        String healthLevel = null;
        Integer score = null;
        Map<String, Object> snapshot = null;

        if (data != null) {
            snapshot = evaluationService.buildAssessmentSnapshot(data, userId, null);
            score = (Integer) snapshot.get("score");
            healthLevel = (String) snapshot.get("healthLevel");
            healthType = (String) snapshot.get("constitution");
        }

        List<Question> allQuestions = questionRepository.findByIsActiveOrderBySortOrderAscIdAsc(1);
        List<Question> selectedQuestions = selectQuestions(allQuestions, healthType, healthLevel);
        List<Map<String, Object>> questions = buildQuestionResponse(selectedQuestions);

        result.put("userId", userId);
        result.put("initialScore", score);
        result.put("initialHealthLevel", healthLevel == null ? "未评估" : healthLevel);
        result.put("initialConstitution", healthType == null ? "未评估" : healthType);
        result.put("initialConstitutionDescription",
                healthType == null ? "尚未录入硬性健康指标。" : evaluationService.getConstitutionDescription(healthType));
        result.put("recommendReason", buildRecommendReason(healthType, healthLevel, selectedQuestions));
        if (snapshot != null) {
            result.put("assessmentSnapshot", snapshot);
        }
        result.put("questions", questions);
        return result;
    }

    @Transactional
    public Map<String, Object> submit(Long userId, QuestionnaireSubmitRequest request) {
        List<UserAnswer> answers = request == null || request.getAnswers() == null
                ? Collections.emptyList()
                : request.getAnswers();

        answerRepository.deleteByUserId(userId);
        for (UserAnswer answer : answers) {
            answer.setUserId(userId);
            answerRepository.save(answer);
        }

        QuestionnaireQuality quality = buildAndSaveQuality(userId, answers, request);
        String questionnaireType = evaluationService.getQuestionnaireConstitution(userId);
        Map<String, Integer> questionnaireScores = evaluationService.getQuestionnaireConstitutionScores(userId);
        Map<String, Integer> dimensionScores = buildQuestionnaireDimensionScores(answers);
        Map<String, Object> dimensionExplain = buildDimensionExplain(dimensionScores);

        HealthData data = healthDataRepository.findTopByUserIdOrderByIdDesc(userId);

        String healthType = null;
        Integer score = null;
        String level = null;
        String riskLevel = null;
        Map<String, Object> snapshot = null;

        if (data != null) {
            snapshot = evaluationService.buildAssessmentSnapshot(data, userId, userId);
            healthType = (String) snapshot.get("constitution");
            score = (Integer) snapshot.get("score");
            level = (String) snapshot.get("healthLevel");
            riskLevel = (String) snapshot.get("riskLevel");
        }

        int questionnaireOverallScore = calculateQuestionnaireOverallScore(dimensionScores);
        int filledIndicatorCount = countFilledIndicatorFields(data);
        int missingIndicatorCount = TOTAL_HARD_INDICATOR_FIELDS - filledIndicatorCount;
        boolean useFusionDecision = data != null && missingIndicatorCount <= 1 && score != null;

        int finalScore = useFusionDecision
                ? (int) Math.round(score * 0.55D + questionnaireOverallScore * 0.45D)
                : questionnaireOverallScore;
        String finalLevel = evaluationService.getLevel(finalScore);
        String finalRiskLevel = evaluationService.getRiskLevel(finalScore);

        Map<String, Integer> healthScores = snapshot == null
                ? Collections.emptyMap()
                : (Map<String, Integer>) snapshot.getOrDefault("constitutionScores", Collections.emptyMap());
        Map<String, Integer> mergedScores = useFusionDecision
                ? evaluationService.mergeConstitutionScores(questionnaireScores, healthScores)
                : new LinkedHashMap<>(questionnaireScores);
        String questionnaireDominant = questionnaireType == null
                ? evaluationService.getTopConstitution(questionnaireScores)
                : questionnaireType;
        String primaryConstitution = useFusionDecision
                ? evaluationService.getTopConstitution(mergedScores)
                : questionnaireDominant;
        Map<String, Object> indicatorScores = snapshot == null
                ? Collections.emptyMap()
                : castMap(snapshot.getOrDefault("indicatorScores", Collections.emptyMap()));
        Map<String, Object> healthProfile = useFusionDecision
                ? evaluationService.buildHealthProfile(
                    finalScore,
                    finalRiskLevel,
                    dimensionScores,
                    mergedScores,
                    indicatorScores
                )
                : buildQuestionnaireDrivenProfile(questionnaireDominant, finalRiskLevel, dimensionScores);
        String finalType = useFusionDecision
                ? String.valueOf(healthProfile.getOrDefault("profileName", primaryConstitution))
                : questionnaireDominant;
        String finalDescription = useFusionDecision
                ? String.valueOf(healthProfile.getOrDefault(
                    "description",
                    evaluationService.getConstitutionDescription(primaryConstitution)
                ))
                : evaluationService.getConstitutionDescription(questionnaireDominant);

        HealthPlan plan = null;
        if (finalLevel != null) {
            plan = healthPlanRepository.findTopByConstitutionTypeAndHealthLevelOrderByCreateTimeDesc(primaryConstitution, finalLevel);
            if (plan == null) {
                plan = healthPlanRepository.findTopByConstitutionTypeOrderByCreateTimeDesc(primaryConstitution);
            }
        }

        if (data != null) {
            HealthResult result = new HealthResult();
            result.setUserId(userId);
            result.setScore((double) finalScore);
            result.setHealthLevel(finalLevel);
            result.setConstitutionType(finalType);
            healthResultRepository.save(result);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("phase", "final");
        response.put("score", finalScore);
        response.put("hardIndicatorScore", score);
        response.put("questionnaireOverallScore", questionnaireOverallScore);
        response.put("healthLevel", finalLevel == null ? "未评估" : finalLevel);
        response.put("riskLevel", finalRiskLevel);
        response.put("hardIndicatorFilledCount", filledIndicatorCount);
        response.put("hardIndicatorMissingCount", missingIndicatorCount);
        response.put("fusionEnabled", useFusionDecision);
        response.put("finalDecisionMode", useFusionDecision ? "FUSION" : "QUESTIONNAIRE");
        response.put("finalDecisionModeLabel", useFusionDecision ? "融合判定" : "问卷主导判定");
        response.put("finalDecisionReason", useFusionDecision
                ? "本次硬性指标填写较完整，因此最终结果采用硬性指标与问卷综合融合的判定方式。"
                : "本次硬性指标缺失超过 1 项，因此最终结果以问卷调查结果为主，硬性指标仅作为辅助分析参考。");
        response.put("healthConstitution", healthType == null ? "未录入" : healthType);
        response.put("healthConstitutionDescription",
                healthType == null ? "尚未录入硬性健康指标。" : evaluationService.getConstitutionDescription(healthType));
        response.put("questionnaireConstitution", questionnaireType == null ? "未评估" : questionnaireType);
        response.put("questionnaireConstitutionDescription",
                questionnaireType == null ? "尚未完成问卷评测。" : evaluationService.getConstitutionDescription(questionnaireType));
        response.put("finalConstitution", finalType);
        response.put("finalConstitutionDescription", finalDescription);
        response.put("primaryConstitution", primaryConstitution);
        response.put("primaryConstitutionDescription", evaluationService.getConstitutionDescription(primaryConstitution));
        response.put("healthProfile", healthProfile);
        response.put("questionnaireConstitutionScores", questionnaireScores);
        response.put("healthConstitutionScores", healthScores);
        response.put("finalConstitutionScores", mergedScores);
        response.put("questionnaireDimensionScores", dimensionScores);
        response.put("dimensionExplain", dimensionExplain);
        response.put("questionnaireQuality", qualityToMap(quality));
        response.put("explainability", buildExplainability(
                snapshot,
                questionnaireScores,
                mergedScores,
                dimensionExplain,
                finalType,
                primaryConstitution,
                useFusionDecision,
                filledIndicatorCount,
                missingIndicatorCount
        ));
        response.put("summary", "最终健康画像为：" + finalType + "（主导体质：" + primaryConstitution + "）");

        if (plan != null) {
            response.put("healthPlan", buildPlanMap(plan, primaryConstitution, finalLevel, finalRiskLevel, finalScore, dimensionScores));
        } else {
            response.put("healthPlan", null);
        }

        if (snapshot != null) {
            response.put("assessmentSnapshot", snapshot);
            response.put("dimensions", snapshot.get("dimensions"));
            response.put("bmi", snapshot.get("bmi"));
            response.put("bodyFatRate", snapshot.get("bodyFatRate"));
            response.put("basalMetabolism", snapshot.get("basalMetabolism"));
        }

        return response;
    }

    public List<Map<String, Object>> getAllQuestionsWithOptions() {
        return buildQuestionResponse(questionRepository.findByIsActiveOrderBySortOrderAscIdAsc(1));
    }

    private QuestionnaireQuality buildAndSaveQuality(Long userId,
                                                     List<UserAnswer> answers,
                                                     QuestionnaireSubmitRequest request) {
        int total = Math.max(TARGET_QUESTION_COUNT, answers.size());
        int answered = (int) answers.stream().filter(item -> item.getOptionId() != null).count();
        int missing = Math.max(0, total - answered);
        int duration = request == null || request.getDurationSeconds() == null ? 0 : request.getDurationSeconds();
        double straightLineRate = calculateStraightLineRate(answers);

        List<String> warnings = new ArrayList<>();
        int qualityScore = 100;
        if (missing > 0) {
            qualityScore -= missing * 8;
            warnings.add("存在未作答题目，结果可信度下降。");
        }
        if (duration > 0 && duration < total * 3) {
            qualityScore -= 18;
            warnings.add("答题速度过快，建议确认是否认真阅读题目。");
        }
        if (straightLineRate >= 0.75) {
            qualityScore -= 18;
            warnings.add("大量题目选择相同选项，可能存在机械作答。");
        }
        if (answers.size() < 20) {
            qualityScore -= 16;
            warnings.add("有效答题数量偏少，建议完成完整问卷。");
        }
        qualityScore = Math.max(0, Math.min(100, qualityScore));

        QuestionnaireQuality quality = new QuestionnaireQuality();
        quality.setUserId(userId);
        quality.setDurationSeconds(duration);
        quality.setTotalQuestions(total);
        quality.setAnsweredQuestions(answered);
        quality.setMissingCount(missing);
        quality.setStraightLineRate(Math.round(straightLineRate * 1000D) / 10D);
        quality.setQualityScore(qualityScore);
        quality.setConsistencyLevel(qualityScore >= 85 ? "高" : qualityScore >= 70 ? "中" : "低");
        quality.setWarnings(warnings.isEmpty() ? "问卷完成质量良好。" : String.join("；", warnings));
        return questionnaireQualityRepository.save(quality);
    }

    private double calculateStraightLineRate(List<UserAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return 0D;
        }
        Map<Long, Integer> counter = new HashMap<>();
        for (UserAnswer answer : answers) {
            if (answer.getOptionId() != null) {
                counter.put(answer.getOptionId(), counter.getOrDefault(answer.getOptionId(), 0) + 1);
            }
        }
        int max = counter.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        return max / (double) answers.size();
    }

    private Map<String, Integer> buildQuestionnaireDimensionScores(List<UserAnswer> answers) {
        Map<String, List<Integer>> grouped = new LinkedHashMap<>();
        for (UserAnswer answer : answers) {
            Question question = questionRepository.findById(answer.getQuestionId()).orElse(null);
            QuestionOption option = optionRepository.findById(answer.getOptionId()).orElse(null);
            if (question == null || option == null || question.getDimension() == null) continue;
            grouped.computeIfAbsent(question.getDimension(), key -> new ArrayList<>()).add(option.getScore());
        }

        Map<String, Integer> result = new LinkedHashMap<>();
        for (String dimension : DIMENSIONS) {
            List<Integer> values = grouped.getOrDefault(dimension, Collections.emptyList());
            int score = values.isEmpty()
                    ? 0
                    : (int) Math.round(values.stream().mapToInt(Integer::intValue).average().orElse(3D) / 5D * 100D);
            result.put(dimension, score);
        }
        return result;
    }

    private Map<String, Object> buildDimensionExplain(Map<String, Integer> dimensionScores) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> ranking = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dimensionScores.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("dimension", entry.getKey());
            item.put("score", entry.getValue());
            item.put("reason", buildDimensionReason(entry.getKey(), entry.getValue()));
            ranking.add(item);
        }
        ranking.sort((a, b) -> Integer.compare((Integer) b.get("score"), (Integer) a.get("score")));
        result.put("ranking", ranking);
        result.put("highestDimension", ranking.isEmpty() ? null : ranking.get(0));
        result.put("lowestDimension", ranking.isEmpty() ? null : ranking.get(ranking.size() - 1));
        return result;
    }

    private int calculateQuestionnaireOverallScore(Map<String, Integer> dimensionScores) {
        if (dimensionScores == null || dimensionScores.isEmpty()) {
            return 70;
        }
        return (int) Math.round(dimensionScores.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(70D));
    }

    private String buildDimensionReason(String dimension, int score) {
        String level = score >= 80 ? "表现较好" : score >= 60 ? "存在一定波动" : "需要重点改善";
        return dimension + "得分为 " + score + "，" + level + "，该维度会参与最终体质和康养方案的综合判断。";
    }

    private Map<String, Object> buildExplainability(Map<String, Object> snapshot,
                                                    Map<String, Integer> questionnaireScores,
                                                    Map<String, Integer> mergedScores,
                                                    Map<String, Object> dimensionExplain,
                                                    String finalType,
                                                    String primaryConstitution,
                                                    boolean useFusionDecision,
                                                    int filledIndicatorCount,
                                                    int missingIndicatorCount) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("finalTypeReason", useFusionDecision
                ? "本次硬性指标已填写 " + filledIndicatorCount + " 项，仅缺失 " + missingIndicatorCount
                + " 项，因此最终健康画像由硬性指标与问卷结果融合判定得到。中医主导体质为 " + primaryConstitution
                + "，最终呈现为 " + finalType + "。"
                : "本次硬性指标仅填写 " + filledIndicatorCount + " 项，缺失 " + missingIndicatorCount
                + " 项，未达到融合判定条件，因此最终结果以问卷调查为主，当前主导体质为 " + primaryConstitution + "。");
        result.put("hardIndicatorReasons", snapshot == null ? Collections.emptyList() : snapshot.get("riskFactors"));
        result.put("questionnaireRanking", evaluationService.buildTopEntries(questionnaireScores, 4));
        result.put("finalRanking", evaluationService.buildTopEntries(mergedScores, 4));
        result.put("dimensionExplain", dimensionExplain);
        result.put("planReason", useFusionDecision
                ? "系统会优先根据主导体质、风险等级和维度短板组合饮食、运动、睡眠和心理放松模板，因此同一主导体质用户也会得到差异化建议。"
                : "由于本次最终结果采用问卷主导判定，康养方案会优先参考问卷体现出的主导体质、风险等级和最低维度短板进行推荐。");
        return result;
    }

    private int countFilledIndicatorFields(HealthData data) {
        if (data == null) {
            return 0;
        }
        int count = 0;
        if (data.getBloodPressureHigh() != null) count++;
        if (data.getBloodPressureLow() != null) count++;
        if (data.getBloodSugar() != null) count++;
        if (data.getHeartRate() != null) count++;
        if (data.getBloodOxygen() != null) count++;
        if (data.getHeight() != null) count++;
        if (data.getWeight() != null) count++;
        return count;
    }

    private Map<String, Object> buildQuestionnaireDrivenProfile(String questionnaireType,
                                                                String riskLevel,
                                                                Map<String, Integer> dimensionScores) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map.Entry<String, Integer> lowest = dimensionScores.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .orElse(null);
        String lowestDimension = lowest == null ? null : lowest.getKey();
        int lowestScore = lowest == null ? 0 : lowest.getValue();
        result.put("profileName", questionnaireType);
        result.put("focus", lowestDimension == null ? "问卷综合判断" : "重点关注" + lowestDimension);
        result.put("description", "由于本次硬性指标填写不完整，最终结果主要依据问卷调查的体质倾向和各维度表现生成，更能反映你当前主观状态和生活方式特征。");
        result.put("riskLevel", riskLevel);
        result.put("lowestDimension", lowestDimension);
        result.put("lowestDimensionScore", lowestScore);
        result.put("primaryConstitution", questionnaireType);
        return result;
    }

    private Map<String, Object> buildPlanMap(HealthPlan plan,
                                             String finalType,
                                             String level,
                                             String riskLevel,
                                             Integer score,
                                             Map<String, Integer> dimensions) {
        Map<String, Object> planMap = new LinkedHashMap<>();
        planMap.put("id", plan.getId());
        planMap.put("title", plan.getTitle());
        planMap.put("diet", plan.getDiet());
        planMap.put("drink", plan.getDrink());
        planMap.put("sport", plan.getSport());
        planMap.put("lifestyle", plan.getLifestyle());
        planMap.put("mealAdvice", chooseTemplate("DIET", finalType, riskLevel, "饮食", score, plan.getDiet()));
        planMap.put("exerciseAdvice", chooseTemplate("SPORT", finalType, riskLevel, "运动", getDimensionScore(dimensions, "生活习惯评估"), plan.getSport()));
        planMap.put("sleepAdvice", chooseTemplate("SLEEP", finalType, riskLevel, "睡眠", getDimensionScore(dimensions, "睡眠质量评估"), plan.getLifestyle()));
        planMap.put("stressAdvice", chooseTemplate("PSYCHOLOGY", finalType, riskLevel, "心理压力", getDimensionScore(dimensions, "心理健康状况"), "每天预留 10-15 分钟做呼吸训练、短步行或正念放松。"));
        planMap.put("recommendationReasons", buildPlanReasons(finalType, riskLevel, dimensions));
        return planMap;
    }

    private String chooseTemplate(String type,
                                  String constitution,
                                  String riskLevel,
                                  String dimension,
                                  Integer score,
                                  String fallback) {
        List<HealthPlanTemplate> templates = healthPlanTemplateRepository
                .findByTemplateTypeAndIsActiveOrderByPriorityDescCreateTimeDesc(type, 1);
        for (HealthPlanTemplate template : templates) {
            if (!matchesTemplate(template, constitution, riskLevel, dimension, score)) continue;
            return template.getContent();
        }
        return fallback;
    }

    private boolean matchesTemplate(HealthPlanTemplate template,
                                    String constitution,
                                    String riskLevel,
                                    String dimension,
                                    Integer score) {
        boolean constitutionMatched = isUniversal(template.getConstitutionType()) || template.getConstitutionType().equals(constitution);
        boolean riskMatched = isUniversal(template.getRiskLevel()) || template.getRiskLevel().equals(riskLevel);
        boolean dimensionMatched = isUniversal(template.getDimension()) || template.getDimension().equals(dimension);
        boolean scoreMatched = score == null
                || ((template.getMinScore() == null || score >= template.getMinScore())
                && (template.getMaxScore() == null || score <= template.getMaxScore()));
        return constitutionMatched && riskMatched && dimensionMatched && scoreMatched;
    }

    private List<String> buildPlanReasons(String finalType, String riskLevel, Map<String, Integer> dimensions) {
        List<String> reasons = new ArrayList<>();
        reasons.add("最终体质为 " + finalType + "，因此优先匹配该体质对应的调养方向。");
        reasons.add("当前风险等级为 " + (riskLevel == null ? "未评估" : riskLevel) + "，方案强度会按风险等级进行控制。");
        Map.Entry<String, Integer> lowest = dimensions.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .orElse(null);
        if (lowest != null) {
            reasons.add("问卷中相对薄弱的维度是 " + lowest.getKey() + "，得分 " + lowest.getValue() + "，因此方案会强化该方面建议。");
        }
        return reasons;
    }

    private int getDimensionScore(Map<String, Integer> dimensions, String key) {
        return dimensions.getOrDefault(key, 70);
    }

    private Map<String, Object> qualityToMap(QuestionnaireQuality quality) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("durationSeconds", quality.getDurationSeconds());
        result.put("totalQuestions", quality.getTotalQuestions());
        result.put("answeredQuestions", quality.getAnsweredQuestions());
        result.put("missingCount", quality.getMissingCount());
        result.put("straightLineRate", quality.getStraightLineRate());
        result.put("qualityScore", quality.getQualityScore());
        result.put("consistencyLevel", quality.getConsistencyLevel());
        result.put("warnings", quality.getWarnings());
        return result;
    }

    private String buildRecommendReason(String healthType, String healthLevel, List<Question> questions) {
        if (healthType == null) {
            return "当前尚未形成初评体质，系统会优先推荐通用题目覆盖四个评估维度。";
        }
        return "系统根据初评体质“" + healthType + "”和健康等级“" + healthLevel + "”抽取题目，并保证身体、生活、心理、睡眠四个维度覆盖。";
    }

    private List<Question> selectQuestions(List<Question> allQuestions, String constitution, String healthLevel) {
        if (allQuestions == null || allQuestions.isEmpty()) return Collections.emptyList();

        List<Question> selected = new ArrayList<>();
        Set<Long> selectedIds = new HashSet<>();

        for (String dimension : DIMENSIONS) {
            int limit = getDimensionLimit(dimension);
            List<Question> candidates = new ArrayList<>();
            for (Question question : allQuestions) {
                if (!dimension.equals(question.getDimension())) continue;
                if (!matches(question, constitution, healthLevel)) continue;
                candidates.add(question);
            }
            candidates.sort((a, b) -> {
                int scoreCompare = Integer.compare(matchScore(b, constitution, healthLevel), matchScore(a, constitution, healthLevel));
                if (scoreCompare != 0) return scoreCompare;
                int sortCompare = Integer.compare(defaultInt(a.getSortOrder()), defaultInt(b.getSortOrder()));
                if (sortCompare != 0) return sortCompare;
                return Long.compare(a.getId(), b.getId());
            });
            for (Question candidate : candidates) {
                if (selectedIds.contains(candidate.getId())) continue;
                selected.add(candidate);
                selectedIds.add(candidate.getId());
                if (countByDimension(selected, dimension) >= limit) break;
            }
        }

        if (selected.size() < TARGET_QUESTION_COUNT) {
            fillRemainingQuestions(selected, selectedIds, allQuestions, constitution, healthLevel, true);
        }
        if (selected.size() < TARGET_QUESTION_COUNT) {
            fillRemainingQuestions(selected, selectedIds, allQuestions, constitution, healthLevel, false);
        }
        return selected.isEmpty() ? allQuestions : selected;
    }

    private List<Map<String, Object>> buildQuestionResponse(List<Question> questions) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Question question : questions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", question.getId());
            item.put("content", question.getContent());
            item.put("category", question.getCategory());
            item.put("dimension", question.getDimension());
            item.put("applicableConstitution", question.getApplicableConstitution());
            item.put("applicableHealthLevel", question.getApplicableHealthLevel());
            item.put("sortOrder", question.getSortOrder());
            item.put("options", optionRepository.findByQuestionId(question.getId()));
            result.add(item);
        }
        return result;
    }

    private boolean matches(Question question, String constitution, String healthLevel) {
        return matchConstitution(question.getApplicableConstitution(), constitution)
                && matchLevel(question.getApplicableHealthLevel(), healthLevel);
    }

    private int matchScore(Question question, String constitution, String healthLevel) {
        int score = 0;
        if ("平和体质".equals(constitution)) {
            if (!isUniversal(question.getApplicableConstitution()) && !"平和体质".equals(question.getApplicableConstitution())) score += 2;
            else if (isExact(question.getApplicableConstitution(), constitution)) score += 1;
            else if (isUniversal(question.getApplicableConstitution())) score += 1;
        } else {
            if (isExact(question.getApplicableConstitution(), constitution)) score += 2;
            else if (isUniversal(question.getApplicableConstitution())) score += 1;
        }
        if (isExact(question.getApplicableHealthLevel(), healthLevel)) score += 2;
        else if (isUniversal(question.getApplicableHealthLevel())) score += 1;
        return score;
    }

    private boolean matchConstitution(String expected, String actual) {
        if ("平和体质".equals(actual)) {
            return true;
        }
        return isUniversal(expected) || (actual != null && expected.equals(actual));
    }

    private boolean matchLevel(String expected, String actual) {
        return isUniversal(expected) || (actual != null && expected.equals(actual));
    }

    private boolean isExact(String expected, String actual) {
        return expected != null && actual != null && expected.equals(actual);
    }

    private boolean isUniversal(String value) {
        return value == null || value.isBlank() || "通用".equals(value) || "ALL".equalsIgnoreCase(value);
    }

    private int getDimensionLimit(String dimension) {
        if ("身体健康状况".equals(dimension)) return 8;
        if ("生活习惯评估".equals(dimension)) return 8;
        if ("心理健康状况".equals(dimension)) return 7;
        if ("睡眠质量评估".equals(dimension)) return 7;
        return 0;
    }

    private int countByDimension(List<Question> questions, String dimension) {
        int count = 0;
        for (Question question : questions) {
            if (dimension.equals(question.getDimension())) count++;
        }
        return count;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private Map<String, Object> castMap(Object value) {
        if (value instanceof Map<?, ?> source) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : source.entrySet()) {
                if (entry.getKey() != null) {
                    result.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            return result;
        }
        return Collections.emptyMap();
    }

    private void fillRemainingQuestions(List<Question> selected,
                                        Set<Long> selectedIds,
                                        List<Question> allQuestions,
                                        String constitution,
                                        String healthLevel,
                                        boolean onlyMatched) {
        List<Question> remainings = new ArrayList<>(allQuestions);
        remainings.sort((a, b) -> {
            int scoreCompare = Integer.compare(matchScore(b, constitution, healthLevel), matchScore(a, constitution, healthLevel));
            if (scoreCompare != 0) return scoreCompare;
            int sortCompare = Integer.compare(defaultInt(a.getSortOrder()), defaultInt(b.getSortOrder()));
            if (sortCompare != 0) return sortCompare;
            return Long.compare(a.getId(), b.getId());
        });

        for (Question question : remainings) {
            if (selected.size() >= TARGET_QUESTION_COUNT) return;
            if (selectedIds.contains(question.getId())) continue;
            if (onlyMatched && !matches(question, constitution, healthLevel)) continue;
            selected.add(question);
            selectedIds.add(question.getId());
        }
    }
}
