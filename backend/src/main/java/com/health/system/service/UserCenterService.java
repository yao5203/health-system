package com.health.system.service;

import com.health.system.dto.DailyCheckinRequest;
import com.health.system.dto.FeedbackRequest;
import com.health.system.dto.PlanFavoriteRequest;
import com.health.system.entity.DailyCheckin;
import com.health.system.entity.Feedback;
import com.health.system.entity.HealthArticle;
import com.health.system.entity.HealthData;
import com.health.system.entity.HealthResult;
import com.health.system.entity.PlanFavorite;
import com.health.system.entity.User;
import com.health.system.repository.DailyCheckinRepository;
import com.health.system.repository.FeedbackRepository;
import com.health.system.repository.HealthArticleRepository;
import com.health.system.repository.HealthDataRepository;
import com.health.system.repository.HealthResultRepository;
import com.health.system.repository.PlanFavoriteRepository;
import com.health.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserCenterService {

    @Autowired
    private HealthDataRepository healthDataRepository;

    @Autowired
    private HealthResultRepository healthResultRepository;

    @Autowired
    private PlanFavoriteRepository planFavoriteRepository;

    @Autowired
    private DailyCheckinRepository dailyCheckinRepository;

    @Autowired
    private HealthArticleRepository healthArticleRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EvaluationService evaluationService;

    public Map<String, Object> getDashboard(Long userId, String range) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        HealthData latestData = healthDataRepository.findTopByUserIdOrderByIdDesc(userId);
        HealthResult latestResult = healthResultRepository.findTopByUserIdOrderByCreateTimeDesc(userId);
        int days = "month".equalsIgnoreCase(range) ? 30 : 7;
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1L);
        List<HealthData> allHealthData = healthDataRepository.findByUserIdOrderByCreateTimeDesc(userId);
        List<HealthResult> allResults = healthResultRepository.findByUserIdOrderByCreateTimeDesc(userId);
        List<DailyCheckin> checkinsInRange = dailyCheckinRepository.findByUserIdAndCheckinDateBetweenOrderByCheckinDateAsc(userId, start, end);

        List<HealthData> dailyLatestData = pickLatestHealthDataPerDay(allHealthData, start);
        List<HealthResult> dailyLatestResults = pickLatestHealthResultPerDay(allResults, start);

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("user", buildUserMap(user));
        dashboard.put("summary", buildSummary(userId, dailyLatestData, dailyLatestResults, checkinsInRange, latestData, latestResult, days));
        dashboard.put("history", buildHistory(dailyLatestData, checkinsInRange, days));
        dashboard.put("favorites", planFavoriteRepository.findByUserIdOrderByCreateTimeDesc(userId));
        dashboard.put("checkins", dailyCheckinRepository.findByUserIdOrderByCheckinDateDescCreateTimeDesc(userId)
                .stream().limit(10).collect(Collectors.toList()));
        dashboard.put("weeklyMonthly", buildWeeklyMonthlySummary(userId));
        dashboard.put("featuredArticles", healthArticleRepository.findByStatusOrderByCreateTimeDesc(1)
                .stream().limit(4).collect(Collectors.toList()));
        dashboard.put("feedbacks", feedbackRepository.findByUserIdOrderByCreateTimeDesc(userId)
                .stream().limit(5).collect(Collectors.toList()));
        return dashboard;
    }

    public List<HealthArticle> listArticles(String category, String keyword) {
        String normalizedCategory = category == null ? "" : category.trim();
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        return healthArticleRepository.searchPublished(1, normalizedCategory, normalizedKeyword);
    }

    public HealthArticle getArticle(Long id) {
        return healthArticleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资讯不存在"));
    }

    public Map<String, Object> getArticleHome(Long userId, Long seed) {
        List<HealthArticle> published = new ArrayList<>(healthArticleRepository.findByStatusOrderByCreateTimeDesc(1));
        Random random = new Random(seed == null ? System.currentTimeMillis() : seed);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("guessYouLike", pickGuessYouLike(userId, published, random));
        result.put("hotCategories", buildHotCategories(published, random));
        result.put("latestArticles", published.stream().limit(4).collect(Collectors.toList()));
        return result;
    }

    public List<PlanFavorite> listFavorites(Long userId) {
        return planFavoriteRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public Map<String, Object> saveFavorite(PlanFavoriteRequest request) {
        if (request.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (request.getPlanId() != null && planFavoriteRepository.existsByUserIdAndPlanId(request.getUserId(), request.getPlanId())) {
            return Map.of("success", false, "message", "该康养方案已收藏");
        }
        PlanFavorite favorite = new PlanFavorite();
        favorite.setUserId(request.getUserId());
        favorite.setPlanId(request.getPlanId());
        favorite.setPlanTitle(request.getPlanTitle());
        favorite.setConstitutionType(request.getConstitutionType());
        favorite.setHealthLevel(request.getHealthLevel());
        favorite.setDiet(request.getDiet());
        favorite.setDrink(request.getDrink());
        favorite.setSport(request.getSport());
        favorite.setLifestyle(request.getLifestyle());
        planFavoriteRepository.save(favorite);
        return Map.of("success", true, "message", "收藏成功");
    }

    public Map<String, Object> removeFavorite(Long userId, Long planId) {
        planFavoriteRepository.deleteByUserIdAndPlanId(userId, planId);
        return Map.of("success", true, "message", "已取消收藏");
    }

    public List<DailyCheckin> listCheckins(Long userId) {
        return dailyCheckinRepository.findByUserIdOrderByCheckinDateDescCreateTimeDesc(userId);
    }

    public DailyCheckin saveCheckin(DailyCheckinRequest request) {
        DailyCheckin checkin = new DailyCheckin();
        checkin.setUserId(request.getUserId());
        checkin.setCheckinDate(request.getCheckinDate());
        checkin.setPlanTitle(request.getPlanTitle());
        checkin.setSleepHours(request.getSleepHours());
        checkin.setExerciseMinutes(request.getExerciseMinutes());
        checkin.setStressLevel(request.getStressLevel());
        checkin.setMoodScore(request.getMoodScore());
        checkin.setWeight(request.getWeight());
        checkin.setBloodPressureHigh(request.getBloodPressureHigh());
        checkin.setBloodPressureLow(request.getBloodPressureLow());
        checkin.setRemark(request.getRemark());
        return dailyCheckinRepository.save(checkin);
    }

    public List<Feedback> listFeedbacks(Long userId) {
        return feedbackRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public Feedback saveFeedback(FeedbackRequest request) {
        if (request.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new RuntimeException("反馈内容不能为空");
        }
        Feedback feedback = new Feedback();
        feedback.setUserId(request.getUserId());
        feedback.setContent(request.getContent().trim());
        return feedbackRepository.save(feedback);
    }

    private Map<String, Object> buildSummary(Long userId,
                                             List<HealthData> dailyLatestData,
                                             List<HealthResult> dailyLatestResults,
                                             List<DailyCheckin> checkinsInRange,
                                             HealthData latestData,
                                             HealthResult latestResult,
                                             int days) {
        Map<String, Object> summary = new LinkedHashMap<>();
        if ((dailyLatestData == null || dailyLatestData.isEmpty()) && latestData == null) {
            summary.put("hasData", false);
            return summary;
        }

        List<HealthData> sourceData = (dailyLatestData == null || dailyLatestData.isEmpty())
                ? Collections.singletonList(latestData)
                : dailyLatestData;
        HealthData aggregatedData = aggregateHealthData(sourceData, userId);
        Map<String, Object> snapshot = evaluationService.buildAssessmentSnapshot(aggregatedData, userId, userId);
        Map<String, Integer> rangeDimensions = evaluationService.calculateRangeDimensionScores(aggregatedData, checkinsInRange, dailyLatestResults);
        snapshot.put("dimensions", rangeDimensions);
        Map<String, Object> healthProfile = castMap(snapshot.get("healthProfile"));
        if (!healthProfile.isEmpty()) {
            snapshot.put("constitution", healthProfile.getOrDefault("profileName", snapshot.get("constitution")));
            snapshot.put("constitutionDescription", healthProfile.getOrDefault("description", snapshot.get("constitutionDescription")));
        }
        snapshot.put("rangeLabel", days == 30 ? "近 30 天融合结果" : "近 7 天融合结果");

        summary.put("hasData", true);
        summary.put("snapshot", snapshot);
        summary.put("latestResult", latestResult);
        summary.put("recentResult", latestResult);
        summary.put("metrics", buildMetricCards(snapshot, days, sourceData.size()));
        return summary;
    }

    private List<HealthArticle> pickGuessYouLike(Long userId, List<HealthArticle> published, Random random) {
        HealthResult latestResult = healthResultRepository.findTopByUserIdOrderByCreateTimeDesc(userId);
        List<String> preferredCategories = new ArrayList<>();

        if (latestResult != null) {
            String constitution = latestResult.getConstitutionType();
            if ("湿热体质".equals(constitution)) {
                preferredCategories.add("饮食指南");
                preferredCategories.add("心理放松");
            } else if ("气虚体质".equals(constitution)) {
                preferredCategories.add("养生知识");
                preferredCategories.add("睡眠管理");
            } else if ("阴虚体质".equals(constitution)) {
                preferredCategories.add("睡眠管理");
                preferredCategories.add("心理放松");
            } else {
                preferredCategories.add("养生知识");
                preferredCategories.add("饮食指南");
            }
        } else {
            preferredCategories.add("养生知识");
            preferredCategories.add("慢病预防");
        }

        List<HealthArticle> preferred = published.stream()
                .filter(article -> preferredCategories.contains(article.getCategory()))
                .collect(Collectors.toList());

        Collections.shuffle(preferred, random);
        if (preferred.size() >= 4) {
            return preferred.subList(0, 4);
        }

        List<HealthArticle> remaining = new ArrayList<>(published);
        remaining.removeAll(preferred);
        Collections.shuffle(remaining, random);
        preferred.addAll(remaining.stream().limit(Math.max(0, 4 - preferred.size())).collect(Collectors.toList()));
        return preferred;
    }

    private List<Map<String, Object>> buildHotCategories(List<HealthArticle> published, Random random) {
        Map<String, List<HealthArticle>> grouped = published.stream()
                .collect(Collectors.groupingBy(HealthArticle::getCategory, LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<HealthArticle>> entry : grouped.entrySet()) {
            List<HealthArticle> articles = new ArrayList<>(entry.getValue());
            Collections.shuffle(articles, random);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("category", entry.getKey());
            item.put("count", entry.getValue().size());
            item.put("articles", articles.stream().limit(2).collect(Collectors.toList()));
            result.add(item);
        }

        result.sort((a, b) -> Integer.compare(
                ((Number) b.get("count")).intValue(),
                ((Number) a.get("count")).intValue()
        ));
        return result.stream().limit(3).collect(Collectors.toList());
    }

    private List<Map<String, Object>> buildMetricCards(Map<String, Object> snapshot, int days, int sampleCount) {
        List<Map<String, Object>> cards = new ArrayList<>();
        String rangeText = days == 30 ? "近 30 天" : "近 7 天";
        cards.add(metric("BMI", snapshot.get("bmi"), rangeText + "内每日最后一次测评融合均值"));
        cards.add(metric("体脂率", snapshot.get("bodyFatRate"), rangeText + "内估算体脂融合值"));
        cards.add(metric("基础代谢", snapshot.get("basalMetabolism"), rangeText + "融合后的静息代谢估算"));
        cards.add(metric("风险等级", snapshot.get("riskLevel"), "基于 " + sampleCount + " 天有效样本的综合风险判断"));
        cards.add(metric("周期体质", snapshot.get("constitution"), "当前周期融合结论，按本周期健康画像统一判定"));
        return cards;
    }

    private Map<String, Object> metric(String label, Object value, String note) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        item.put("note", note);
        return item;
    }

    private Map<String, Object> buildHistory(List<HealthData> dailyLatestData,
                                             List<DailyCheckin> checkins,
                                             int days) {
        Map<String, Object> history = new LinkedHashMap<>();
        history.put("range", days == 30 ? "month" : "week");
        history.put("weightTrend", buildHealthDataTrend(dailyLatestData, "weight"));
        history.put("pressureTrend", buildPressureTrend(dailyLatestData));
        history.put("sleepTrend", buildSleepTrend(checkins));
        history.put("healthDataList", dailyLatestData.stream().limit(12).collect(Collectors.toList()));
        return history;
    }

    private List<Map<String, Object>> buildHealthDataTrend(List<HealthData> allHealthData, String field) {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (HealthData data : allHealthData) {
            if (data.getCreateTime() == null) {
                continue;
            }
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", data.getCreateTime().format(formatter));
            point.put("value", "weight".equals(field) ? data.getWeight() : data.getBloodSugar());
            result.add(point);
        }
        return result;
    }

    private List<Map<String, Object>> buildPressureTrend(List<HealthData> allHealthData) {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (HealthData data : allHealthData) {
            if (data.getCreateTime() == null) {
                continue;
            }
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", data.getCreateTime().format(formatter));
            point.put("high", data.getBloodPressureHigh());
            point.put("low", data.getBloodPressureLow());
            result.add(point);
        }
        return result;
    }

    private List<Map<String, Object>> buildSleepTrend(List<DailyCheckin> checkins) {
        Map<LocalDate, DailyCheckin> byDate = new HashMap<>();
        for (DailyCheckin item : checkins) {
            byDate.put(item.getCheckinDate(), item);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<LocalDate> orderedDates = new ArrayList<>(byDate.keySet());
        Collections.sort(orderedDates);
        for (LocalDate cursor : orderedDates) {
            DailyCheckin item = byDate.get(cursor);
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", cursor.format(formatter));
            point.put("value", item == null ? null : item.getSleepHours());
            result.add(point);
        }
        return result;
    }

    private List<HealthData> pickLatestHealthDataPerDay(List<HealthData> allHealthData, LocalDate start) {
        Map<LocalDate, HealthData> latestByDay = new LinkedHashMap<>();
        for (HealthData item : allHealthData) {
            if (item.getCreateTime() == null || item.getCreateTime().toLocalDate().isBefore(start)) {
                continue;
            }
            LocalDate date = item.getCreateTime().toLocalDate();
            HealthData current = latestByDay.get(date);
            if (current == null || item.getCreateTime().isAfter(current.getCreateTime())) {
                latestByDay.put(date, item);
            }
        }
        return latestByDay.values().stream()
                .sorted(Comparator.comparing(HealthData::getCreateTime))
                .collect(Collectors.toList());
    }

    private List<HealthResult> pickLatestHealthResultPerDay(List<HealthResult> allResults, LocalDate start) {
        Map<LocalDate, HealthResult> latestByDay = new LinkedHashMap<>();
        for (HealthResult item : allResults) {
            if (item.getCreateTime() == null || item.getCreateTime().toLocalDate().isBefore(start)) {
                continue;
            }
            LocalDate date = item.getCreateTime().toLocalDate();
            HealthResult current = latestByDay.get(date);
            if (current == null || item.getCreateTime().isAfter(current.getCreateTime())) {
                latestByDay.put(date, item);
            }
        }
        return latestByDay.values().stream()
                .sorted(Comparator.comparing(HealthResult::getCreateTime))
                .collect(Collectors.toList());
    }

    private HealthData aggregateHealthData(List<HealthData> list, Long userId) {
        HealthData aggregated = new HealthData();
        aggregated.setUserId(userId);
        aggregated.setBloodPressureHigh(roundNullableInteger(list.stream().map(HealthData::getBloodPressureHigh).collect(Collectors.toList())));
        aggregated.setBloodPressureLow(roundNullableInteger(list.stream().map(HealthData::getBloodPressureLow).collect(Collectors.toList())));
        aggregated.setBloodSugar(averageDoubleOrNull(list.stream().map(HealthData::getBloodSugar).collect(Collectors.toList())));
        aggregated.setHeartRate(roundNullableInteger(list.stream().map(HealthData::getHeartRate).collect(Collectors.toList())));
        aggregated.setBloodOxygen(averageDoubleOrNull(list.stream().map(HealthData::getBloodOxygen).collect(Collectors.toList())));
        aggregated.setHeight(averageDoubleOrNull(list.stream().map(HealthData::getHeight).collect(Collectors.toList())));
        aggregated.setWeight(averageDoubleOrNull(list.stream().map(HealthData::getWeight).collect(Collectors.toList())));
        HealthData latest = list.get(list.size() - 1);
        aggregated.setBloodPressureContext(latest.getBloodPressureContext());
        aggregated.setBloodSugarContext(latest.getBloodSugarContext());
        aggregated.setHeartRateContext(latest.getHeartRateContext());
        aggregated.setBloodOxygenContext(latest.getBloodOxygenContext());
        aggregated.setCreateTime(latest.getCreateTime());
        return aggregated;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }

    private Double averageDoubleOrNull(List<Double> values) {
        List<Double> valid = values.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (valid.isEmpty()) {
            return null;
        }
        return Math.round(valid.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0D) * 10.0) / 10.0;
    }

    private Integer roundNullableInteger(List<Integer> values) {
        List<Integer> valid = values.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (valid.isEmpty()) {
            return null;
        }
        return (int) Math.round(valid.stream().mapToInt(Integer::intValue).average().orElse(0D));
    }

    private Map<String, Object> buildWeeklyMonthlySummary(Long userId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("weekly", summarizeByDays(userId, 7));
        result.put("monthly", summarizeByDays(userId, 30));
        return result;
    }

    private Map<String, Object> summarizeByDays(Long userId, int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1L);
        List<DailyCheckin> checkins = dailyCheckinRepository.findByUserIdAndCheckinDateBetweenOrderByCheckinDateAsc(userId, start, end);

        double avgSleep = checkins.stream()
                .filter(item -> item.getSleepHours() != null)
                .mapToDouble(DailyCheckin::getSleepHours)
                .average()
                .orElse(0D);
        double avgStress = checkins.stream()
                .filter(item -> item.getStressLevel() != null)
                .mapToInt(DailyCheckin::getStressLevel)
                .average()
                .orElse(0D);
        double avgExercise = checkins.stream()
                .filter(item -> item.getExerciseMinutes() != null)
                .mapToInt(DailyCheckin::getExerciseMinutes)
                .average()
                .orElse(0D);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("checkinCount", checkins.size());
        result.put("avgSleepHours", round(avgSleep));
        result.put("avgStressLevel", round(avgStress));
        result.put("avgExerciseMinutes", round(avgExercise));
        return result;
    }

    private Map<String, Object> buildUserMap(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("age", user.getAge());
        map.put("gender", user.getGender());
        map.put("phone", user.getPhone());
        return map;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
