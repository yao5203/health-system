package com.health.system.controller;

import com.health.system.dto.*;
import com.health.system.entity.ChangeProposal;
import com.health.system.entity.HealthPlan;
import com.health.system.entity.RuleConfig;
import com.health.system.service.AdminService;
import com.health.system.service.ConsultationService;
import com.health.system.service.DoctorAdminService;
import com.health.system.service.DoctorService;
import com.health.system.service.ProposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private DoctorAdminService doctorAdminService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private ProposalService proposalService;

    @Autowired
    private ConsultationService consultationService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody AdminLoginRequest request) {
        return adminService.login(request);
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return doctorAdminService.getDashboardStats();
    }

    @GetMapping("/users")
    public Object listUsers() {
        return doctorAdminService.listUsers();
    }

    @GetMapping("/doctors")
    public Object listDoctors() {
        return doctorService.listDoctors();
    }

    @GetMapping("/rules")
    public Object listRules() {
        return doctorAdminService.listRules();
    }

    @PostMapping("/rules")
    public RuleConfig saveRule(@RequestBody RuleConfigRequest request,
                               @RequestParam(defaultValue = "admin") String operator) {
        return doctorAdminService.saveRule(request, operator);
    }

    @GetMapping("/plans")
    public Object listPlans() {
        return doctorAdminService.listPlans();
    }

    @GetMapping("/plans/{id}")
    public HealthPlan getPlan(@PathVariable Long id) {
        return doctorAdminService.getPlan(id);
    }

    @PostMapping("/plans")
    public HealthPlan createPlan(@RequestBody HealthPlan plan,
                                 @RequestParam(defaultValue = "admin") String operator) {
        return doctorAdminService.createPlan(plan, operator);
    }

    @PutMapping("/plans/{id}")
    public HealthPlan updatePlan(@PathVariable Long id,
                                 @RequestBody HealthPlan plan,
                                 @RequestParam(defaultValue = "admin") String operator) {
        return doctorAdminService.updatePlan(id, plan, operator);
    }

    @DeleteMapping("/plans/{id}")
    public Map<String, String> deletePlan(@PathVariable Long id,
                                          @RequestParam(defaultValue = "admin") String operator) {
        doctorAdminService.deletePlan(id, operator);
        return Map.of("message", "删除成功");
    }

    @GetMapping("/questions")
    public Object listQuestions() {
        return doctorAdminService.listQuestions();
    }

    @PostMapping("/questions")
    public Object createQuestion(@RequestBody QuestionRequest request,
                                 @RequestParam(defaultValue = "admin") String operator) {
        return doctorAdminService.createQuestion(request, operator);
    }

    @PutMapping("/questions/{id}")
    public Object updateQuestion(@PathVariable Long id,
                                 @RequestBody QuestionRequest request,
                                 @RequestParam(defaultValue = "admin") String operator) {
        return doctorAdminService.updateQuestion(id, request, operator);
    }

    @DeleteMapping("/questions/{id}")
    public Map<String, String> deleteQuestion(@PathVariable Long id,
                                              @RequestParam(defaultValue = "admin") String operator) {
        doctorAdminService.deleteQuestion(id, operator);
        return Map.of("message", "删除成功");
    }

    @GetMapping("/articles")
    public Object listArticles() {
        return doctorAdminService.listArticles();
    }

    @PostMapping("/articles")
    public Object createArticle(@RequestBody ArticleRequest request,
                                @RequestParam(defaultValue = "admin") String operator) {
        return doctorAdminService.createArticle(request, operator);
    }

    @PutMapping("/articles/{id}")
    public Object updateArticle(@PathVariable Long id,
                                @RequestBody ArticleRequest request,
                                @RequestParam(defaultValue = "admin") String operator) {
        return doctorAdminService.updateArticle(id, request, operator);
    }

    @DeleteMapping("/articles/{id}")
    public Map<String, String> deleteArticle(@PathVariable Long id,
                                             @RequestParam(defaultValue = "admin") String operator) {
        doctorAdminService.deleteArticle(id, operator);
        return Map.of("message", "删除成功");
    }

    @GetMapping("/health-data")
    public Object listHealthData(@RequestParam(required = false) Long userId) {
        return doctorAdminService.listHealthData(userId);
    }

    @GetMapping("/results")
    public Object listResults(@RequestParam(required = false) Long userId) {
        return doctorAdminService.listHealthResults(userId);
    }

    @GetMapping("/feedbacks")
    public Object listFeedbacks() {
        return doctorAdminService.listFeedbacks();
    }

    @GetMapping("/proposals")
    public Object listProposals(@RequestParam(required = false) String status) {
        return proposalService.listAll(status);
    }

    @PostMapping("/proposals/{proposalId}/review")
    public ChangeProposal reviewProposal(@PathVariable Long proposalId,
                                         @RequestBody ProposalReviewRequest request) {
        return proposalService.review(proposalId, request);
    }

    @GetMapping("/consultations")
    public Object listConsultations() {
        return consultationService.listAll();
    }

    @PostMapping("/consultations/{consultationId}/assign")
    public Object assignConsultation(@PathVariable Long consultationId,
                                     @RequestBody ConsultationAssignRequest request) {
        return consultationService.assign(consultationId, request);
    }

    @GetMapping("/doctor-matches")
    public Object listRecommendedDoctors(@RequestParam(required = false) String preferredTag) {
        return consultationService.listRecommendedDoctors(preferredTag);
    }
}
