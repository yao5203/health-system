package com.health.system.controller;

import com.health.system.dto.ConsultationMessageRequest;
import com.health.system.dto.DoctorLoginRequest;
import com.health.system.dto.DoctorProfileRequest;
import com.health.system.dto.DoctorRegisterRequest;
import com.health.system.dto.ProposalSubmitRequest;
import com.health.system.entity.ChangeProposal;
import com.health.system.entity.ConsultationMessage;
import com.health.system.service.DoctorAdminService;
import com.health.system.service.ConsultationService;
import com.health.system.service.DoctorService;
import com.health.system.service.ProposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorAdminService doctorAdminService;

    @Autowired
    private ProposalService proposalService;

    @Autowired
    private ConsultationService consultationService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody DoctorRegisterRequest request) {
        return doctorService.register(request);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody DoctorLoginRequest request) {
        return doctorService.login(request);
    }

    @GetMapping("/profile/{doctorId}")
    public Map<String, Object> getProfile(@PathVariable Long doctorId) {
        return doctorService.getDoctor(doctorId);
    }

    @PutMapping("/profile/{doctorId}")
    public Map<String, Object> updateProfile(@PathVariable Long doctorId,
                                             @RequestBody DoctorProfileRequest request) {
        return doctorService.updateProfile(doctorId, request);
    }

    @GetMapping("/overview/{doctorId}")
    public Map<String, Object> getOverview(@PathVariable Long doctorId) {
        return doctorService.getWorkspaceOverview(doctorId);
    }

    @GetMapping("/directory")
    public List<Map<String, Object>> listDoctors() {
        return doctorService.listDoctors();
    }

    @GetMapping("/plans")
    public Object listPlans() {
        return doctorAdminService.listPlans();
    }

    @GetMapping("/articles")
    public Object listArticles() {
        return doctorAdminService.listArticles();
    }

    @PostMapping("/proposals")
    public ChangeProposal submitProposal(@RequestBody ProposalSubmitRequest request) {
        return proposalService.submit(request);
    }

    @GetMapping("/proposals")
    public List<ChangeProposal> listMyProposals(@RequestParam Long doctorId) {
        return proposalService.listByDoctor(doctorId);
    }

    @GetMapping("/consultations")
    public List<Map<String, Object>> listConsultations(@RequestParam Long doctorId) {
        return consultationService.listByDoctor(doctorId);
    }

    @GetMapping("/consultations/{consultationId}/messages")
    public List<ConsultationMessage> listMessages(@PathVariable Long consultationId) {
        return consultationService.listMessages(consultationId);
    }

    @GetMapping("/consultations/{consultationId}/snapshot")
    public Map<String, Object> getConsultationSnapshot(@PathVariable Long consultationId,
                                                       @RequestParam Long doctorId) {
        return consultationService.getDoctorConsultationSnapshot(consultationId, doctorId);
    }

    @PostMapping("/consultations/{consultationId}/messages")
    public ConsultationMessage sendMessage(@PathVariable Long consultationId,
                                           @RequestBody ConsultationMessageRequest request) {
        return consultationService.sendMessage(consultationId, request);
    }

    @PostMapping("/consultations/{consultationId}/close")
    public Object closeConsultation(@PathVariable Long consultationId,
                                    @RequestParam(defaultValue = "doctor") String operator) {
        return consultationService.close(consultationId, operator);
    }
}
