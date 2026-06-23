package com.health.system.controller;

import com.health.system.dto.QuestionnaireSubmitRequest;
import com.health.system.service.QuestionnaireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/question")
public class QuestionnaireController {

    @Autowired
    private QuestionnaireService service;

    // ✅ 获取题目 + 选项
    @GetMapping("/list")
    public List<Map<String, Object>> list() {
        return service.getAllQuestionsWithOptions();
    }

    @GetMapping("/recommend")
    public Map<String, Object> recommend(@RequestParam Long userId) {
        return service.getRecommendedQuestions(userId);
    }

    // ✅ 提交问卷
    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestParam Long userId,
                                      @RequestBody QuestionnaireSubmitRequest request) {

        return service.submit(userId, request);
    }
}
