package com.health.system.controller;

import com.health.system.entity.HealthData;
import com.health.system.service.HealthDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthDataController {

    @Autowired
    private HealthDataService service;

    @PostMapping("/add")
    public Map<String, Object> add(@RequestBody HealthData data) {
        return service.add(data);
    }
}
