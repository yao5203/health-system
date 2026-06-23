package com.health.system.controller;

import com.health.system.dto.DailyCheckinRequest;
import com.health.system.dto.FeedbackRequest;
import com.health.system.dto.ConsultationApplyRequest;
import com.health.system.dto.ConsultationMessageRequest;
import com.health.system.dto.PlanFavoriteRequest;
import com.health.system.entity.ConsultationMessage;
import com.health.system.entity.DailyCheckin;
import com.health.system.entity.Feedback;
import com.health.system.entity.HealthArticle;
import com.health.system.entity.PlanFavorite;
import com.health.system.service.ConsultationService;
import com.health.system.service.UserCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user-center")
public class UserCenterController {

    @Autowired
    private UserCenterService userCenterService;

    @Autowired
    private ConsultationService consultationService;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(@RequestParam Long userId,
                                         @RequestParam(defaultValue = "week") String range) {
        return userCenterService.getDashboard(userId, range);
    }

    @GetMapping("/articles")
    public List<HealthArticle> listArticles(@RequestParam(required = false) String category,
                                            @RequestParam(required = false) String keyword) {
        return userCenterService.listArticles(category, keyword);
    }

    @GetMapping("/articles/home")
    public Map<String, Object> articleHome(@RequestParam Long userId,
                                           @RequestParam(required = false) Long seed) {
        return userCenterService.getArticleHome(userId, seed);
    }

    @GetMapping("/articles/{id}")
    public HealthArticle getArticle(@PathVariable Long id) {
        return userCenterService.getArticle(id);
    }

    @GetMapping("/favorites")
    public List<PlanFavorite> listFavorites(@RequestParam Long userId) {
        return userCenterService.listFavorites(userId);
    }

    @PostMapping("/favorites")
    public Map<String, Object> saveFavorite(@RequestBody PlanFavoriteRequest request) {
        return userCenterService.saveFavorite(request);
    }

    @DeleteMapping("/favorites/{planId}")
    public Map<String, Object> deleteFavorite(@PathVariable Long planId,
                                              @RequestParam Long userId) {
        return userCenterService.removeFavorite(userId, planId);
    }

    @GetMapping("/checkins")
    public List<DailyCheckin> listCheckins(@RequestParam Long userId) {
        return userCenterService.listCheckins(userId);
    }

    @PostMapping("/checkins")
    public DailyCheckin saveCheckin(@RequestBody DailyCheckinRequest request) {
        return userCenterService.saveCheckin(request);
    }

    @GetMapping("/feedbacks")
    public List<Feedback> listFeedbacks(@RequestParam Long userId) {
        return userCenterService.listFeedbacks(userId);
    }

    @PostMapping("/feedbacks")
    public Feedback saveFeedback(@RequestBody FeedbackRequest request) {
        return userCenterService.saveFeedback(request);
    }

    @GetMapping("/consultations")
    public List<Map<String, Object>> listConsultations(@RequestParam Long userId) {
        return consultationService.listByUser(userId);
    }

    @PostMapping("/consultations")
    public Object applyConsultation(@RequestBody ConsultationApplyRequest request) {
        return consultationService.apply(request);
    }

    @GetMapping("/consultations/{consultationId}/messages")
    public List<ConsultationMessage> listConsultationMessages(@PathVariable Long consultationId) {
        return consultationService.listMessages(consultationId);
    }

    @PostMapping("/consultations/{consultationId}/messages")
    public ConsultationMessage sendConsultationMessage(@PathVariable Long consultationId,
                                                       @RequestBody ConsultationMessageRequest request) {
        return consultationService.sendMessage(consultationId, request);
    }
}
