package com.health.system.controller;

import com.health.system.dto.DoctorQuestionView;
import com.health.system.service.DoctorAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctor/questions")
public class DoctorQuestionController {

    @Autowired
    private DoctorAdminService doctorAdminService;

    @GetMapping
    public List<DoctorQuestionView> listQuestions() {
        return doctorAdminService.listQuestions();
    }

    @GetMapping("/{id}")
    public DoctorQuestionView getQuestion(@PathVariable Long id) {
        return doctorAdminService.getQuestion(id);
    }
}
