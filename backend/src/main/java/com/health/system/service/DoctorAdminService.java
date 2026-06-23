package com.health.system.service;

import com.health.system.dto.ArticleRequest;
import com.health.system.dto.DoctorQuestionView;
import com.health.system.dto.QuestionOptionRequest;
import com.health.system.dto.QuestionRequest;
import com.health.system.dto.RuleConfigRequest;
import com.health.system.dto.UserHealthDataView;
import com.health.system.dto.UserHealthResultView;
import com.health.system.entity.*;
import com.health.system.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DoctorAdminService {

    private static final List<RuleSeed> DEFAULT_RULES = List.of(
            new RuleSeed("risk_normal_score", "正常风险阈值", "88", "综合评分高于等于该值时判定为正常"),
            new RuleSeed("risk_mild_subhealth_score", "轻度亚健康阈值", "76", "综合评分高于等于该值时判定为轻度亚健康"),
            new RuleSeed("risk_subhealth_score", "中度亚健康阈值", "64", "综合评分高于等于该值时判定为中度亚健康"),
            new RuleSeed("risk_medium_score", "偏高风险阈值", "50", "综合评分高于等于该值时判定为偏高风险"),
            new RuleSeed("risk_high_score", "高风险阈值", "0", "综合评分低于偏高风险阈值且高于等于该值时判定为高风险")
    );

    @Autowired
    private HealthPlanRepository healthPlanRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    @Autowired
    private HealthDataRepository healthDataRepository;

    @Autowired
    private HealthResultRepository healthResultRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HealthArticleRepository healthArticleRepository;

    @Autowired
    private RuleConfigRepository ruleConfigRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private SystemLogService systemLogService;

    @Autowired
    private EvaluationService evaluationService;

    public List<HealthPlan> listPlans() {
        return healthPlanRepository.findAll();
    }

    public HealthPlan getPlan(Long id) {
        return healthPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("康养方案不存在"));
    }

    public HealthPlan createPlan(HealthPlan plan, String operator) {
        HealthPlan saved = healthPlanRepository.save(plan);
        systemLogService.saveLog("新增康养方案:" + saved.getTitle(), operator);
        return saved;
    }

    public HealthPlan updatePlan(Long id, HealthPlan plan, String operator) {
        HealthPlan existing = getPlan(id);
        existing.setTitle(plan.getTitle());
        existing.setConstitutionType(plan.getConstitutionType());
        existing.setHealthLevel(plan.getHealthLevel());
        existing.setDiet(plan.getDiet());
        existing.setDrink(plan.getDrink());
        existing.setSport(plan.getSport());
        existing.setLifestyle(plan.getLifestyle());

        HealthPlan saved = healthPlanRepository.save(existing);
        systemLogService.saveLog("修改康养方案:" + saved.getTitle(), operator);
        return saved;
    }

    public void deletePlan(Long id, String operator) {
        HealthPlan existing = getPlan(id);
        healthPlanRepository.delete(existing);
        systemLogService.saveLog("删除康养方案:" + existing.getTitle(), operator);
    }

    public List<DoctorQuestionView> listQuestions() {
        List<Question> questions = questionRepository.findAllByOrderBySortOrderAscIdAsc();
        List<DoctorQuestionView> result = new ArrayList<>();
        for (Question question : questions) {
            List<QuestionOption> options = questionOptionRepository.findByQuestionId(question.getId());
            result.add(DoctorQuestionView.from(question, options));
        }
        return result;
    }

    public DoctorQuestionView getQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("题目不存在"));
        List<QuestionOption> options = questionOptionRepository.findByQuestionId(question.getId());
        return DoctorQuestionView.from(question, options);
    }

    @Transactional
    public DoctorQuestionView createQuestion(QuestionRequest request, String operator) {
        validateQuestionRequest(request);

        Question question = new Question();
        question.setContent(request.getContent());
        question.setType(request.getType());
        question.setCategory(request.getCategory());
        question.setDimension(request.getDimension());
        question.setApplicableConstitution(request.getApplicableConstitution());
        question.setApplicableHealthLevel(request.getApplicableHealthLevel());
        question.setSortOrder(request.getSortOrder());
        question.setIsActive(request.getIsActive());
        Question savedQuestion = questionRepository.save(question);

        saveQuestionOptions(savedQuestion.getId(), request.getOptions());

        systemLogService.saveLog("新增问卷题目:" + savedQuestion.getContent(), operator);
        return getQuestion(savedQuestion.getId());
    }

    @Transactional
    public DoctorQuestionView updateQuestion(Long id, QuestionRequest request, String operator) {
        validateQuestionRequest(request);

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("题目不存在"));
        question.setContent(request.getContent());
        question.setType(request.getType());
        question.setCategory(request.getCategory());
        question.setDimension(request.getDimension());
        question.setApplicableConstitution(request.getApplicableConstitution());
        question.setApplicableHealthLevel(request.getApplicableHealthLevel());
        question.setSortOrder(request.getSortOrder());
        question.setIsActive(request.getIsActive());
        questionRepository.save(question);

        questionOptionRepository.deleteByQuestionId(id);
        saveQuestionOptions(id, request.getOptions());

        systemLogService.saveLog("修改问卷题目:" + question.getContent(), operator);
        return getQuestion(id);
    }

    @Transactional
    public void deleteQuestion(Long id, String operator) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("题目不存在"));
        questionOptionRepository.deleteByQuestionId(id);
        questionRepository.delete(question);
        systemLogService.saveLog("删除问卷题目:" + question.getContent(), operator);
    }

    public List<UserHealthDataView> listHealthData(Long userId) {
        List<HealthData> healthDataList = userId == null
                ? healthDataRepository.findAllByOrderByCreateTimeDesc()
                : healthDataRepository.findByUserIdOrderByCreateTimeDesc(userId);

        List<UserHealthDataView> result = new ArrayList<>();
        for (HealthData data : healthDataList) {
            User user = userRepository.findById(data.getUserId()).orElse(null);
            result.add(UserHealthDataView.from(
                    data,
                    user == null ? null : user.getUsername(),
                    user == null ? null : user.getAge(),
                    user == null ? null : user.getGender(),
                    user == null ? null : user.getPhone()
            ));
        }
        return result;
    }

    public List<UserHealthResultView> listHealthResults(Long userId) {
        List<HealthResult> results = userId == null
                ? healthResultRepository.findAll()
                : healthResultRepository.findByUserIdOrderByCreateTimeDesc(userId);

        results.sort(Comparator.comparing(HealthResult::getCreateTime,
                Comparator.nullsLast(Comparator.reverseOrder())));

        List<UserHealthResultView> views = new ArrayList<>();
        for (HealthResult result : results) {
            User user = userRepository.findById(result.getUserId()).orElse(null);
            views.add(UserHealthResultView.from(
                    result,
                    user == null ? null : user.getUsername(),
                    user == null ? null : user.getAge(),
                    user == null ? null : user.getGender(),
                    user == null ? null : user.getPhone()
            ));
        }
        return views;
    }

    public List<Map<String, Object>> listUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", user.getId());
            item.put("username", user.getUsername());
            item.put("age", user.getAge());
            item.put("gender", user.getGender());
            item.put("phone", user.getPhone());
            item.put("createTime", user.getCreateTime());
            HealthResult latestResult = healthResultRepository.findTopByUserIdOrderByCreateTimeDesc(user.getId());
            item.put("latestHealthLevel", latestResult == null ? null : latestResult.getHealthLevel());
            item.put("latestConstitution", latestResult == null ? null : latestResult.getConstitutionType());
            users.add(item);
        }
        users.sort(Comparator.comparing(item -> String.valueOf(item.get("username"))));
        return users;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userCount", userRepository.count());
        result.put("planCount", healthPlanRepository.count());
        result.put("questionCount", questionRepository.count());
        result.put("articleCount", healthArticleRepository.count());
        result.put("healthDataCount", healthDataRepository.count());
        result.put("healthDistribution", buildHealthDistribution());
        result.put("constitutionDistribution", buildConstitutionDistribution());
        return result;
    }

    public List<HealthArticle> listArticles() {
        return healthArticleRepository.findAllByOrderByCreateTimeDesc();
    }

    public HealthArticle createArticle(ArticleRequest request, String operator) {
        HealthArticle article = new HealthArticle();
        applyArticle(article, request);
        HealthArticle saved = healthArticleRepository.save(article);
        systemLogService.saveLog("新增资讯:" + saved.getTitle(), operator);
        return saved;
    }

    public HealthArticle updateArticle(Long id, ArticleRequest request, String operator) {
        HealthArticle article = healthArticleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资讯不存在"));
        applyArticle(article, request);
        HealthArticle saved = healthArticleRepository.save(article);
        systemLogService.saveLog("修改资讯:" + saved.getTitle(), operator);
        return saved;
    }

    public void deleteArticle(Long id, String operator) {
        HealthArticle article = healthArticleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("资讯不存在"));
        healthArticleRepository.delete(article);
        systemLogService.saveLog("删除资讯:" + article.getTitle(), operator);
    }

    public List<RuleConfig> listRules() {
        ensureDefaultRules();
        ensureHealthLevelRules();
        return ruleConfigRepository.findAllByOrderByRuleKeyAsc()
                .stream()
                .filter(item -> !"doctor_register_secret".equals(item.getRuleKey()))
                .toList();
    }

    public List<Map<String, Object>> listFeedbacks() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Feedback> feedbacks = feedbackRepository.findAll();
        feedbacks.sort(Comparator.comparing(Feedback::getCreateTime,
                Comparator.nullsLast(Comparator.reverseOrder())));

        for (Feedback feedback : feedbacks) {
            User user = userRepository.findById(feedback.getUserId()).orElse(null);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", feedback.getId());
            item.put("userId", feedback.getUserId());
            item.put("username", user == null ? null : user.getUsername());
            item.put("content", feedback.getContent());
            item.put("createTime", feedback.getCreateTime());
            result.add(item);
        }
        return result;
    }

    public RuleConfig saveRule(RuleConfigRequest request, String operator) {
        if (request.getRuleKey() == null || request.getRuleKey().isBlank()) {
            throw new RuntimeException("规则标识不能为空");
        }
        RuleConfig config = ruleConfigRepository.findByRuleKey(request.getRuleKey());
        if (config == null) {
            config = new RuleConfig();
            config.setRuleKey(request.getRuleKey());
        }
        config.setRuleName(request.getRuleName());
        config.setRuleValue(request.getRuleValue());
        config.setDescription(request.getDescription());
        RuleConfig saved = ruleConfigRepository.save(config);
        systemLogService.saveLog("保存评测规则:" + saved.getRuleKey(), operator);
        return saved;
    }

    private void validateQuestionRequest(QuestionRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new RuntimeException("题目内容不能为空");
        }
        if (request.getType() == null) {
            throw new RuntimeException("题目类型不能为空");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new RuntimeException("题目分类不能为空");
        }
        if (request.getDimension() == null || request.getDimension().isBlank()) {
            throw new RuntimeException("题目评估维度不能为空");
        }
        if (request.getOptions() == null || request.getOptions().isEmpty()) {
            throw new RuntimeException("题目选项不能为空");
        }
    }

    private void saveQuestionOptions(Long questionId, List<QuestionOptionRequest> options) {
        for (QuestionOptionRequest optionRequest : options) {
            QuestionOption option = new QuestionOption();
            option.setQuestionId(questionId);
            option.setContent(optionRequest.getContent());
            option.setScore(optionRequest.getScore());
            questionOptionRepository.save(option);
        }
    }

    private Map<String, Long> buildHealthDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("正常", 0L);
        distribution.put("轻度亚健康", 0L);
        distribution.put("中度亚健康", 0L);
        distribution.put("偏高风险", 0L);
        distribution.put("高风险", 0L);

        for (HealthResult result : healthResultRepository.findAll()) {
            int score = result.getScore() == null ? 0 : (int) Math.round(result.getScore());
            String bucket = evaluationService.getRiskLevel(score);
            if (!distribution.containsKey(bucket)) {
                distribution.put(bucket, 0L);
            }
            distribution.put(bucket, distribution.get(bucket) + 1);
        }
        return distribution;
    }

    private Map<String, Long> buildConstitutionDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (HealthResult result : healthResultRepository.findAll()) {
            String key = result.getConstitutionType() == null ? "未评估" : result.getConstitutionType();
            distribution.put(key, distribution.getOrDefault(key, 0L) + 1);
        }
        return distribution;
    }

    private void applyArticle(HealthArticle article, ArticleRequest request) {
        article.setTitle(request.getTitle());
        article.setCategory(request.getCategory());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setTags(request.getTags());
        article.setCoverImage(request.getCoverImage());
        article.setStatus(request.getStatus() == null ? 1 : request.getStatus());
    }

    private void ensureDefaultRules() {
        for (RuleSeed seed : DEFAULT_RULES) {
            RuleConfig config = ruleConfigRepository.findByRuleKey(seed.ruleKey());
            if (config == null) {
                config = new RuleConfig();
                config.setRuleKey(seed.ruleKey());
                config.setRuleName(seed.ruleName());
                config.setRuleValue(seed.ruleValue());
                config.setDescription(seed.description());
                ruleConfigRepository.save(config);
            }
        }
    }

    private void ensureHealthLevelRules() {
        ensureRuleIfMissing("level_excellent_score", "优秀健康等级阈值", "88", "综合评分高于等于该值时健康等级判定为优秀");
        ensureRuleIfMissing("level_good_score", "良好健康等级阈值", "76", "综合评分高于等于该值时健康等级判定为良好");
        ensureRuleIfMissing("level_general_score", "一般健康等级阈值", "62", "综合评分高于等于该值时健康等级判定为一般");
        ensureRuleIfMissing("level_risk_score", "风险健康等级阈值", "0", "综合评分低于一般健康等级阈值且高于等于该值时健康等级判定为风险");
    }

    private void ensureRuleIfMissing(String ruleKey, String ruleName, String ruleValue, String description) {
        RuleConfig config = ruleConfigRepository.findByRuleKey(ruleKey);
        if (config != null) {
            return;
        }
        RuleConfig created = new RuleConfig();
        created.setRuleKey(ruleKey);
        created.setRuleName(ruleName);
        created.setRuleValue(ruleValue);
        created.setDescription(description);
        ruleConfigRepository.save(created);
    }

    private record RuleSeed(String ruleKey, String ruleName, String ruleValue, String description) {
    }
}
