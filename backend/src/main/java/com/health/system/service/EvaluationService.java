package com.health.system.service;

import com.health.system.entity.DailyCheckin;
import com.health.system.entity.HealthData;
import com.health.system.entity.HealthResult;
import com.health.system.entity.Question;
import com.health.system.entity.QuestionOption;
import com.health.system.entity.RuleConfig;
import com.health.system.entity.User;
import com.health.system.entity.UserAnswer;
import com.health.system.repository.QuestionOptionRepository;
import com.health.system.repository.QuestionRepository;
import com.health.system.repository.RuleConfigRepository;
import com.health.system.repository.UserAnswerRepository;
import com.health.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EvaluationService {

    private static final Map<String, String> CONSTITUTION_DESCRIPTION = new LinkedHashMap<>();

    static {
        CONSTITUTION_DESCRIPTION.put("平和体质", "平和体质通常表现为精力较充沛、面色较自然、睡眠和食欲相对稳定，整体身体状态比较协调。");
        CONSTITUTION_DESCRIPTION.put("气虚体质", "气虚体质通常表现为容易疲劳、气短乏力、活动后恢复较慢，整体耐力与恢复能力偏弱。");
        CONSTITUTION_DESCRIPTION.put("湿热体质", "湿热体质通常表现为口苦口黏、面部易出油、身体困重、容易烦躁，饮食偏油辣后不适更明显。");
        CONSTITUTION_DESCRIPTION.put("阴虚体质", "阴虚体质通常表现为心烦易燥、口干咽燥、手足心热、睡眠不稳，身体偏于内热和消耗状态。");
    }

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RuleConfigRepository ruleConfigRepository;

    public int calculateScore(HealthData data) {
        double total = 0D;
        double totalWeight = 0D;
        for (Object value : getIndicatorScores(data).values()) {
            Map<String, Object> item = (Map<String, Object>) value;
            total += ((Number) item.get("weightedScore")).doubleValue();
            totalWeight += ((Number) item.get("weight")).doubleValue();
        }
        if (totalWeight <= 0D) {
            return 0;
        }
        return clamp((int) Math.round(total / totalWeight));
    }

    public double calculateBmi(HealthData data) {
        if (data == null || data.getHeight() == null || data.getWeight() == null || data.getHeight() == 0) {
            return 0D;
        }
        return round(data.getWeight() / Math.pow(data.getHeight() / 100, 2));
    }

    public double calculateBodyFatRate(HealthData data, Long userId) {
        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        double bmi = calculateBmi(data);
        if (bmi == 0D) {
            return 0D;
        }
        int age = user == null || user.getAge() == null ? 24 : user.getAge();
        int genderFactor = user != null && "男".equals(user.getGender()) ? 1 : 0;
        return round(1.2 * bmi + 0.23 * age - 10.8 * genderFactor - 5.4);
    }

    public double calculateBasalMetabolism(HealthData data, Long userId) {
        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        if (data == null || data.getHeight() == null || data.getWeight() == null) {
            return 0D;
        }
        int age = user == null || user.getAge() == null ? 24 : user.getAge();
        if (user != null && "男".equals(user.getGender())) {
            return round(10 * data.getWeight() + 6.25 * data.getHeight() - 5 * age + 5);
        }
        return round(10 * data.getWeight() + 6.25 * data.getHeight() - 5 * age - 161);
    }

    public String getLevel(int score) {
        if (score >= getRuleInt("level_excellent_score", 88)) return "优秀";
        if (score >= getRuleInt("level_good_score", 76)) return "良好";
        if (score >= getRuleInt("level_general_score", 62)) return "一般";
        if (score >= getRuleInt("level_risk_score", 0)) return "风险";
        return "风险";
    }

    public String getRiskLevel(int score) {
        if (score >= getRuleInt("risk_normal_score", 88)) return "正常";
        if (score >= getRuleInt("risk_mild_subhealth_score", 76)) return "轻度亚健康";
        if (score >= getRuleInt("risk_subhealth_score", 64)) return "中度亚健康";
        if (score >= getRuleInt("risk_medium_score", 50)) return "偏高风险";
        return "高风险";
    }

    public String getHealthConstitution(HealthData data) {
        return getTopConstitution(calculateConstitutionScores(data));
    }

    public String getQuestionCategoryById(Long questionId) {
        return questionRepository.findById(questionId)
                .map(Question::getCategory)
                .orElse("平和体质");
    }

    public String getQuestionnaireConstitution(Long userId) {
        return getTopConstitution(getQuestionnaireConstitutionScores(userId));
    }

    public Map<String, Integer> getQuestionnaireConstitutionScores(Long userId) {
        List<UserAnswer> answers = userAnswerRepository.findByUserId(userId);
        if (answers == null || answers.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Integer> constitutionScore = new LinkedHashMap<>();
        Map<String, Integer> constitutionCount = new HashMap<>();
        for (UserAnswer answer : answers) {
            QuestionOption option = questionOptionRepository.findById(answer.getOptionId()).orElse(null);
            Question question = questionRepository.findById(answer.getQuestionId()).orElse(null);
            if (option == null || question == null) continue;
            String category = getQuestionCategoryById(answer.getQuestionId());
            int tendencyScore = question.getType() != null && question.getType() == 2
                    ? 6 - option.getScore()
                    : option.getScore();
            constitutionScore.put(category, constitutionScore.getOrDefault(category, 0) + tendencyScore);
            constitutionCount.put(category, constitutionCount.getOrDefault(category, 0) + 1);
        }

        Map<String, Integer> result = new LinkedHashMap<>();
        for (String constitution : CONSTITUTION_DESCRIPTION.keySet()) {
            int total = constitutionScore.getOrDefault(constitution, 0);
            int count = constitutionCount.getOrDefault(constitution, 0);
            if (count == 0) {
                result.put(constitution, 0);
            } else {
                result.put(constitution, clamp((int) Math.round((total / (double) count / 5D) * 100)));
            }
        }
        return result;
    }

    public String mergeConstitution(String questionnaireType, String healthType) {
        Map<String, Integer> questionnaireScores = new LinkedHashMap<>();
        if (questionnaireType != null) questionnaireScores.put(questionnaireType, 100);
        Map<String, Integer> healthScores = new LinkedHashMap<>();
        if (healthType != null) healthScores.put(healthType, 100);
        return getTopConstitution(mergeConstitutionScores(questionnaireScores, healthScores));
    }

    public String getConstitutionDescription(String constitution) {
        return CONSTITUTION_DESCRIPTION.getOrDefault(
                constitution,
                "该体质反映了当前身体状态在代谢、精力、情绪和睡眠等方面的综合倾向，需要结合生活方式持续调整。"
        );
    }

    public Map<String, Object> buildAssessmentSnapshot(HealthData data, Long userId, Long questionnaireUserId) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> indicatorScores = getIndicatorScores(data);
        int score = calculateScore(data);
        Map<String, Integer> dimensions = calculateDimensionScores(data, questionnaireUserId);
        Map<String, Integer> constitutionScores = calculateConstitutionScores(data);
        String constitution = getTopConstitution(constitutionScores);

        result.put("score", score);
        result.put("healthLevel", getLevel(score));
        result.put("riskLevel", getRiskLevel(score));
        result.put("bmi", calculateBmi(data));
        result.put("bodyFatRate", calculateBodyFatRate(data, userId));
        result.put("basalMetabolism", calculateBasalMetabolism(data, userId));
        result.put("dimensions", dimensions);
        result.put("dimensionWeights", buildDimensionWeights(dimensions));
        result.put("constitution", constitution);
        result.put("constitutionDescription", getConstitutionDescription(constitution));
        result.put("constitutionScores", constitutionScores);
        result.put("indicatorScores", indicatorScores);
        result.put("measuredIndicatorCount", indicatorScores.size());
        result.put("riskFactors", buildRiskFactors(indicatorScores));
        result.put("constitutionExplanation", buildConstitutionExplanation(constitutionScores, indicatorScores));
        result.put("healthProfile", buildHealthProfile(score, getRiskLevel(score), dimensions, constitutionScores, indicatorScores));
        return result;
    }

    public Map<String, Object> buildHealthProfile(Integer score,
                                                  String riskLevel,
                                                  Map<String, Integer> dimensions,
                                                  Map<String, Integer> constitutionScores,
                                                  Map<String, Object> indicatorScores) {
        String profileName = "平衡稳态型";
        String focus = "维持稳定";
        String description = "整体指标较平稳，当前重点是保持规律饮食、运动和睡眠，避免长期不良习惯造成风险积累。";

        String lowestDimension = dimensions == null || dimensions.isEmpty()
                ? null
                : dimensions.entrySet().stream().min(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
        int lowestScore = lowestDimension == null ? 100 : dimensions.getOrDefault(lowestDimension, 100);

        int bloodSugarScore = getIndicatorScore(indicatorScores, "血糖");
        int bloodPressureScore = getIndicatorScore(indicatorScores, "血压");
        int bmiScore = getIndicatorScore(indicatorScores, "BMI");
        int heartRateScore = getIndicatorScore(indicatorScores, "心率");
        int oxygenScore = getIndicatorScore(indicatorScores, "血氧");

        if (bloodSugarScore < 75 || bmiScore < 72) {
            profileName = "代谢负担型";
            focus = "控糖控脂与体重管理";
            description = "血糖、BMI 或体脂相关指标提示代谢负担偏高，建议优先调整饮食结构、总热量和运动连续性。";
        } else if (bloodPressureScore < 75 || heartRateScore < 72) {
            profileName = "循环压力型";
            focus = "血压心率与压力管理";
            description = "血压或心率对综合评分影响较明显，说明循环系统和压力状态需要重点关注。";
        } else if (oxygenScore < 78) {
            profileName = "恢复能力关注型";
            focus = "呼吸循环与恢复质量";
            description = "血氧或恢复相关指标偏弱，建议关注睡眠质量、轻量运动和身体恢复。";
        } else if ("睡眠".equals(lowestDimension) || "睡眠质量评估".equals(lowestDimension)) {
            profileName = "睡眠恢复不足型";
            focus = "睡眠节律修复";
            description = "问卷显示睡眠相关维度相对薄弱，建议优先修复入睡节律和夜间恢复质量。";
        } else if ("心理压力".equals(lowestDimension) || "心理健康状况".equals(lowestDimension)) {
            profileName = "压力敏感型";
            focus = "情绪压力调节";
            description = "心理压力维度得分相对较低，说明压力、情绪或紧张状态对健康画像影响较大。";
        } else if ("运动".equals(lowestDimension) || "生活习惯评估".equals(lowestDimension)) {
            profileName = "运动不足型";
            focus = "活动量与体能提升";
            description = "生活习惯和运动维度相对不足，建议从可持续的中低强度运动开始建立规律。";
        } else if ("饮食".equals(lowestDimension) || "身体健康状况".equals(lowestDimension)) {
            profileName = "饮食代谢失衡型";
            focus = "饮食结构优化";
            description = "身体健康和饮食相关维度存在短板，需要优先控制油盐糖、夜宵和总热量。";
        }

        if (score != null && score < 50) {
            profileName = "高风险干预型";
            focus = "多指标风险控制";
            description = "综合评分较低，说明多个指标或维度同时存在风险，需要以安全、低强度、可持续的方式进行干预。";
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("profileName", profileName);
        result.put("focus", focus);
        result.put("description", description);
        result.put("riskLevel", riskLevel);
        result.put("lowestDimension", lowestDimension);
        result.put("lowestDimensionScore", lowestScore);
        result.put("primaryConstitution", getTopConstitution(constitutionScores));
        return result;
    }

    public Map<String, Object> getIndicatorScores(HealthData data) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (data != null && data.getBloodPressureHigh() != null && data.getBloodPressureLow() != null) {
            putIndicator(
                    result,
                    "血压",
                    calculateBloodPressureScore(data.getBloodPressureHigh(), data.getBloodPressureLow(), data.getBloodPressureContext()),
                    0.24,
                    buildBloodPressureReason(data.getBloodPressureContext())
            );
        }
        if (data != null && data.getBloodSugar() != null) {
            putIndicator(
                    result,
                    "血糖",
                    calculateBloodSugarScore(data.getBloodSugar(), data.getBloodSugarContext()),
                    0.22,
                    buildBloodSugarReason(data.getBloodSugarContext())
            );
        }
        if (data != null && data.getHeight() != null && data.getWeight() != null && data.getHeight() > 0) {
            putIndicator(result, "BMI", calculateBmiScore(calculateBmi(data)),
                    0.18, "BMI 用于判断体重与身高是否匹配，会影响运动、饮食和体质倾向判断。");
        }
        if (data != null && data.getHeartRate() != null) {
            putIndicator(
                    result,
                    "心率",
                    calculateHeartRateScore(data.getHeartRate(), data.getHeartRateContext()),
                    0.14,
                    buildHeartRateReason(data.getHeartRateContext())
            );
        }
        if (data != null && data.getBloodOxygen() != null) {
            putIndicator(
                    result,
                    "血氧",
                    calculateBloodOxygenScore(data.getBloodOxygen(), data.getBloodOxygenContext()),
                    0.12,
                    buildBloodOxygenReason(data.getBloodOxygenContext())
            );
        }
        double bodyFatRate = calculateBodyFatRate(data, data == null ? null : data.getUserId());
        if (bodyFatRate > 0D) {
            putIndicator(result, "体脂率", calculateBodyFatScore(bodyFatRate),
                    0.10, "体脂率用于补充 BMI 的不足，帮助区分单纯体重和脂肪比例问题。");
        }
        return result;
    }

    public Map<String, Integer> calculateConstitutionScores(HealthData data) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        double bmi = calculateBmi(data);
        int balanced = 68;
        int qiDeficiency = 38;
        int dampHeat = 38;
        int yinDeficiency = 36;

        if (bmi > 0D) {
            balanced -= (int) Math.round(Math.abs(bmi - 21.5) * 5);
            qiDeficiency += (int) Math.round(Math.max(0, 19.2 - bmi) * 16);
            dampHeat += (int) Math.round(Math.max(0, bmi - 23.5) * 12);
            yinDeficiency += (int) Math.round(Math.max(0, 20.2 - bmi) * 5);
        }
        if (data != null && data.getBloodSugar() != null) {
            double normalizedSugar = normalizeBloodSugarByContext(data.getBloodSugar(), data.getBloodSugarContext());
            balanced -= (int) Math.round(Math.max(0, normalizedSugar - 5.5) * 12);
            dampHeat += (int) Math.round(Math.max(0, normalizedSugar - 5.8) * 15);
        }
        if (data != null && data.getBloodOxygen() != null) {
            balanced -= (int) Math.round(Math.max(0, 95 - data.getBloodOxygen()) * 4);
            qiDeficiency += (int) Math.round(Math.max(0, 95 - data.getBloodOxygen()) * 7);
            yinDeficiency += (int) Math.round(Math.max(0, 96 - data.getBloodOxygen()) * 6);
        }
        if (data != null && data.getHeartRate() != null) {
            int normalizedHeartRate = normalizeHeartRateByContext(data.getHeartRate(), data.getHeartRateContext());
            balanced -= (int) Math.round(Math.max(0, normalizedHeartRate - 88) * 0.9);
            qiDeficiency += (int) Math.round(Math.max(0, 70 - normalizedHeartRate) * 0.3);
            dampHeat += (int) Math.round(Math.max(0, normalizedHeartRate - 85) * 0.4);
            yinDeficiency += (int) Math.round(Math.max(0, normalizedHeartRate - 88) * 1.1);
        }
        if (data != null && data.getBloodPressureHigh() != null && data.getBloodPressureLow() != null) {
            int normalizedHigh = normalizeSystolicByContext(data.getBloodPressureHigh(), data.getBloodPressureContext());
            int normalizedLow = normalizeDiastolicByContext(data.getBloodPressureLow(), data.getBloodPressureContext());
            balanced -= (int) Math.round(Math.max(0, normalizedHigh - 125) * 0.8 + Math.max(0, normalizedLow - 82) * 0.8);
            qiDeficiency += (int) Math.round(Math.max(0, 100 - normalizedHigh) * 0.6 + Math.max(0, 65 - normalizedLow) * 0.8);
            dampHeat += (int) Math.round(Math.max(0, normalizedHigh - 128) * 0.7 + Math.max(0, normalizedLow - 84) * 0.6);
            yinDeficiency += (int) Math.round(Math.max(0, normalizedHigh - 130) * 0.3);
        }

        scores.put("平和体质", balanced);
        scores.put("气虚体质", qiDeficiency);
        scores.put("湿热体质", dampHeat);
        scores.put("阴虚体质", yinDeficiency);
        return scores;
    }

    public Map<String, Integer> mergeConstitutionScores(Map<String, Integer> questionnaireScores,
                                                        Map<String, Integer> healthScores) {
        Map<String, Integer> merged = new LinkedHashMap<>();
        for (String key : CONSTITUTION_DESCRIPTION.keySet()) {
            int questionnaireScore = questionnaireScores.getOrDefault(key, 0);
            int healthScore = healthScores.getOrDefault(key, 0);
            int agreementBonus = questionnaireScore > 0 && healthScore > 0 ? 5 : 0;
            int finalScore = clamp((int) Math.round(questionnaireScore * 0.60 + healthScore * 0.40 + agreementBonus));
            merged.put(key, finalScore);
        }
        return merged;
    }

    public String getTopConstitution(Map<String, Integer> scores) {
        if (scores == null || scores.isEmpty()) {
            return "平和体质";
        }
        return Collections.max(scores.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    public Map<String, Integer> calculateDimensionScores(HealthData data, Long userId) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        int baseScore = calculateScore(data);
        scores.put("体质", clamp(baseScore));

        Map<String, List<Integer>> dimensionOptionScores = new HashMap<>();
        List<UserAnswer> answers = userId == null ? Collections.emptyList() : userAnswerRepository.findByUserId(userId);
        for (UserAnswer answer : answers) {
            Question question = questionRepository.findById(answer.getQuestionId()).orElse(null);
            QuestionOption option = questionOptionRepository.findById(answer.getOptionId()).orElse(null);
            if (question == null || option == null || question.getDimension() == null) continue;
            dimensionOptionScores.computeIfAbsent(question.getDimension(), key -> new ArrayList<>()).add(option.getScore());
        }

        scores.put("睡眠", normalizeDimensionScore(dimensionOptionScores.get("睡眠质量评估"), clamp(55 + safeHeartSleepBoost(data))));
        scores.put("运动", normalizeDimensionScore(dimensionOptionScores.get("生活习惯评估"), clamp(50 + safeExerciseBoost(data))));
        scores.put("饮食", normalizeDimensionScore(dimensionOptionScores.get("身体健康状况"), clamp(50 + safeDietBoost(data))));
        scores.put("心理压力", normalizeDimensionScore(dimensionOptionScores.get("心理健康状况"), clamp(52 + safeStressBoost(data))));
        return scores;
    }

    public Map<String, Integer> calculateRangeDimensionScores(HealthData aggregatedData,
                                                              List<DailyCheckin> checkins,
                                                              List<HealthResult> results) {
        Map<String, Integer> scores = new LinkedHashMap<>();
        int baseScore = calculateScore(aggregatedData);
        scores.put("体质", clamp((int) Math.round(baseScore * 0.55 + averageConstitutionStability(results) * 0.45)));
        scores.put("睡眠", clamp((int) Math.round(averageSleepScore(checkins) * 0.75 + safeHeartSleepBoost(aggregatedData) * 0.25)));
        scores.put("运动", clamp((int) Math.round(averageExerciseScore(checkins) * 0.65 + safeExerciseBoost(aggregatedData) * 0.35)));
        scores.put("饮食", clamp((int) Math.round(averageDietScore(aggregatedData) * 0.7 + baseScore * 0.3)));
        scores.put("心理压力", clamp((int) Math.round(averageStressScore(checkins) * 0.7 + safeStressBoost(aggregatedData) * 0.3)));
        return scores;
    }

    public List<Map<String, Object>> buildTopEntries(Map<String, Integer> scores, int limit) {
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", entry.getKey());
                    item.put("score", entry.getValue());
                    return item;
                })
                .toList();
    }

    private void putIndicator(Map<String, Object> result, String name, double score, double weight, String reason) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("score", clamp((int) Math.round(score)));
        item.put("weight", weight);
        item.put("weightedScore", score * weight);
        item.put("reason", reason);
        result.put(name, item);
    }

    private List<Map<String, Object>> buildRiskFactors(Map<String, Object> indicatorScores) {
        List<Map<String, Object>> factors = new ArrayList<>();
        for (Map.Entry<String, Object> entry : indicatorScores.entrySet()) {
            Map<String, Object> item = (Map<String, Object>) entry.getValue();
            int score = ((Number) item.get("score")).intValue();
            if (score < 82) {
                Map<String, Object> factor = new LinkedHashMap<>();
                factor.put("name", entry.getKey());
                factor.put("score", score);
                factor.put("reason", item.get("reason"));
                factors.add(factor);
            }
        }
        factors.sort(Comparator.comparingInt(item -> ((Number) item.get("score")).intValue()));
        return factors;
    }

    private int getIndicatorScore(Map<String, Object> indicatorScores, String key) {
        if (indicatorScores == null || !indicatorScores.containsKey(key)) {
            return 100;
        }
        Object raw = indicatorScores.get(key);
        if (!(raw instanceof Map<?, ?> map)) {
            return 100;
        }
        Object score = map.get("score");
        return score instanceof Number number ? number.intValue() : 100;
    }

    private Map<String, Object> buildConstitutionExplanation(Map<String, Integer> constitutionScores,
                                                             Map<String, Object> indicatorScores) {
        String top = getTopConstitution(constitutionScores);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("topConstitution", top);
        result.put("topScore", constitutionScores.getOrDefault(top, 0));
        result.put("description", getConstitutionDescription(top));
        result.put("scoreRanking", buildTopEntries(constitutionScores, 4));
        result.put("indicatorReasons", buildRiskFactors(indicatorScores));
        return result;
    }

    private Map<String, Double> buildDimensionWeights(Map<String, Integer> dimensions) {
        Map<String, Double> result = new LinkedHashMap<>();
        double total = dimensions.values().stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) total = 1;
        for (Map.Entry<String, Integer> entry : dimensions.entrySet()) {
            result.put(entry.getKey(), round(entry.getValue() * 100D / total));
        }
        return result;
    }

    private int normalizeDimensionScore(List<Integer> values, int fallback) {
        if (values == null || values.isEmpty()) return fallback;
        double average = values.stream().mapToInt(Integer::intValue).average().orElse(3D);
        return clamp((int) Math.round((average / 5D) * 100));
    }

    private int safeHeartSleepBoost(HealthData data) {
        if (data == null) return 0;
        int boost = 0;
        if (data.getHeartRate() != null && data.getHeartRate() >= 60 && data.getHeartRate() <= 90) boost += 20;
        if (data.getBloodOxygen() != null && data.getBloodOxygen() >= 95) boost += 20;
        return boost;
    }

    private int safeExerciseBoost(HealthData data) {
        if (data == null) return 0;
        int boost = 0;
        double bmi = calculateBmi(data);
        if (bmi > 0D) {
            if (bmi >= 18.5 && bmi <= 24) boost += 28;
            else if (bmi <= 28) boost += 18;
        }
        if (data.getHeartRate() != null && data.getHeartRate() >= 60 && data.getHeartRate() <= 95) boost += 12;
        return boost;
    }

    private int safeDietBoost(HealthData data) {
        if (data == null) return 0;
        int boost = 0;
        if (data.getBloodSugar() != null && data.getBloodSugar() >= 3.9 && data.getBloodSugar() <= 6.1) boost += 26;
        else if (data.getBloodSugar() != null && data.getBloodSugar() <= 7) boost += 16;
        if (data.getBloodPressureHigh() != null && data.getBloodPressureLow() != null
                && data.getBloodPressureHigh() <= 130 && data.getBloodPressureLow() <= 85) boost += 14;
        return boost;
    }

    private int safeStressBoost(HealthData data) {
        if (data == null) return 0;
        int boost = 0;
        if (data.getHeartRate() != null && data.getHeartRate() <= 90) boost += 18;
        if (data.getBloodOxygen() != null && data.getBloodOxygen() >= 95) boost += 12;
        return boost;
    }

    private double calculateBloodPressureScore(Integer high, Integer low, String context) {
        if (high == null || low == null) return 0D;
        int targetHigh = 116;
        int targetLow = 76;
        int normalizedHigh = normalizeSystolicByContext(high, context);
        int normalizedLow = normalizeDiastolicByContext(low, context);
        double systolicPenalty = Math.abs(normalizedHigh - targetHigh) * 0.9;
        double diastolicPenalty = Math.abs(normalizedLow - targetLow) * 1.15;
        if (normalizedHigh >= 140 || normalizedLow >= 90) {
            systolicPenalty += 8;
            diastolicPenalty += 8;
        }
        return clampDouble(100 - systolicPenalty - diastolicPenalty);
    }

    private double calculateBloodSugarScore(Double bloodSugar, String context) {
        if (bloodSugar == null) return 0D;
        double normalized = normalizeBloodSugarByContext(bloodSugar, context);
        if (normalized >= 4.4 && normalized <= 5.8) return 96D;
        if (normalized >= 3.9 && normalized <= 6.1) return 88D;
        if (normalized <= 7.0) return clampDouble(82 - Math.abs(normalized - 5.5) * 20);
        return clampDouble(62 - (normalized - 7.0) * 18);
    }

    private double calculateBmiScore(double bmi) {
        if (bmi == 0D) return 55D;
        if (bmi >= 20 && bmi <= 23.9) return 96D;
        if (bmi >= 18.5 && bmi <= 24.9) return 88D;
        if (bmi <= 28) return clampDouble(78 - Math.abs(bmi - 22.5) * 8);
        return clampDouble(52 - Math.abs(bmi - 28) * 6);
    }

    private double calculateHeartRateScore(Integer heartRate, String context) {
        if (heartRate == null) return 0D;
        int normalized = normalizeHeartRateByContext(heartRate, context);
        if (normalized >= 62 && normalized <= 84) return 95D;
        if (normalized >= 55 && normalized <= 95) return clampDouble(88 - Math.abs(normalized - 74) * 1.7);
        return clampDouble(60 - Math.abs(normalized - 74) * 1.1);
    }

    private double calculateBloodOxygenScore(Double oxygen, String context) {
        if (oxygen == null) return 0D;
        double normalized = normalizeBloodOxygenByContext(oxygen, context);
        if (normalized >= 97) return 97D;
        if (normalized >= 95) return 90D;
        if (normalized >= 92) return clampDouble(82 - (95 - normalized) * 6);
        return clampDouble(50 - (92 - normalized) * 8);
    }

    private double calculateBodyFatScore(double bodyFatRate) {
        if (bodyFatRate == 0D) return 55D;
        if (bodyFatRate >= 12 && bodyFatRate <= 27) return 92D;
        return clampDouble(82 - Math.abs(bodyFatRate - 20) * 2.2);
    }

    private int averageSleepScore(List<DailyCheckin> checkins) {
        if (checkins == null || checkins.isEmpty()) return 62;
        return clamp((int) Math.round(checkins.stream()
                .filter(item -> item.getSleepHours() != null)
                .mapToDouble(item -> {
                    double hours = item.getSleepHours();
                    if (hours >= 7 && hours <= 8.5) return 92D;
                    if (hours >= 6.5 && hours <= 9) return 80D;
                    return Math.max(45D, 75D - Math.abs(hours - 7.5) * 12);
                })
                .average()
                .orElse(62D)));
    }

    private int averageExerciseScore(List<DailyCheckin> checkins) {
        if (checkins == null || checkins.isEmpty()) return 60;
        return clamp((int) Math.round(checkins.stream()
                .filter(item -> item.getExerciseMinutes() != null)
                .mapToDouble(item -> {
                    int minutes = item.getExerciseMinutes();
                    if (minutes >= 30 && minutes <= 70) return 90D;
                    if (minutes >= 15 && minutes < 30) return 75D;
                    if (minutes > 70 && minutes <= 100) return 80D;
                    return Math.max(48D, 65D - Math.abs(minutes - 45) * 0.5);
                })
                .average()
                .orElse(60D)));
    }

    private int averageStressScore(List<DailyCheckin> checkins) {
        if (checkins == null || checkins.isEmpty()) return 63;
        return clamp((int) Math.round(checkins.stream()
                .filter(item -> item.getStressLevel() != null)
                .mapToDouble(item -> Math.max(35D, 100D - item.getStressLevel() * 8D))
                .average()
                .orElse(63D)));
    }

    private int averageDietScore(HealthData data) {
        double total = 0D;
        double weight = 0D;
        if (data != null && data.getBloodSugar() != null) {
            total += calculateBloodSugarScore(data.getBloodSugar(), data.getBloodSugarContext()) * 0.55;
            weight += 0.55;
        }
        if (data != null && data.getBloodPressureHigh() != null && data.getBloodPressureLow() != null) {
            total += calculateBloodPressureScore(data.getBloodPressureHigh(), data.getBloodPressureLow(), data.getBloodPressureContext()) * 0.20;
            weight += 0.20;
        }
        if (data != null && data.getHeight() != null && data.getWeight() != null && data.getHeight() > 0) {
            total += calculateBmiScore(calculateBmi(data)) * 0.25;
            weight += 0.25;
        }
        if (weight <= 0D) {
            return 60;
        }
        return clamp((int) Math.round(total / weight));
    }

    private String buildBloodPressureReason(String context) {
        return "血压按“" + readableContext(context, "静息") + "”场景分析，用于判断循环系统负担与波动风险。";
    }

    private String buildBloodSugarReason(String context) {
        return "血糖按“" + readableContext(context, "空腹") + "”场景分析，避免把空腹值与餐后值混在一起判断。";
    }

    private String buildHeartRateReason(String context) {
        return "心率按“" + readableContext(context, "静息") + "”场景分析，用于评估恢复状态与压力负担。";
    }

    private String buildBloodOxygenReason(String context) {
        return "血氧按“" + readableContext(context, "静息") + "”场景分析，用于辅助判断恢复质量与呼吸循环状态。";
    }

    private String readableContext(String context, String fallback) {
        return (context == null || context.isBlank()) ? fallback : context;
    }

    private double normalizeBloodSugarByContext(Double value, String context) {
        if (value == null) return 0D;
        String normalized = normalizeContext(context);
        if ("POSTPRANDIAL".equals(normalized)) {
            return value - 1.8D;
        }
        if ("RANDOM".equals(normalized)) {
            return value - 1.0D;
        }
        return value;
    }

    private int normalizeHeartRateByContext(Integer value, String context) {
        if (value == null) return 0;
        String normalized = normalizeContext(context);
        if ("AFTER_EXERCISE".equals(normalized)) {
            return Math.max(55, value - 22);
        }
        return value;
    }

    private double normalizeBloodOxygenByContext(Double value, String context) {
        if (value == null) return 0D;
        String normalized = normalizeContext(context);
        if ("AFTER_EXERCISE".equals(normalized)) {
            return value + 1.5D;
        }
        return value;
    }

    private int normalizeSystolicByContext(Integer value, String context) {
        if (value == null) return 0;
        String normalized = normalizeContext(context);
        if ("AFTER_EXERCISE".equals(normalized)) {
            return value - 15;
        }
        return value;
    }

    private int normalizeDiastolicByContext(Integer value, String context) {
        if (value == null) return 0;
        String normalized = normalizeContext(context);
        if ("AFTER_EXERCISE".equals(normalized)) {
            return value - 8;
        }
        return value;
    }

    private String normalizeContext(String context) {
        return context == null ? "" : context.trim().toUpperCase();
    }

    private int averageConstitutionStability(List<HealthResult> results) {
        if (results == null || results.isEmpty()) return 64;
        Map<String, Long> grouped = new HashMap<>();
        for (HealthResult item : results) {
            grouped.put(item.getConstitutionType(), grouped.getOrDefault(item.getConstitutionType(), 0L) + 1);
        }
        long max = grouped.values().stream().mapToLong(Long::longValue).max().orElse(1);
        return clamp((int) Math.round(55 + (max * 45.0 / results.size())));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private double clampDouble(double value) {
        return Math.max(0D, Math.min(100D, value));
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private int getRuleInt(String key, int defaultValue) {
        RuleConfig config = ruleConfigRepository.findByRuleKey(key);
        if (config == null || config.getRuleValue() == null || config.getRuleValue().isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(config.getRuleValue().trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
