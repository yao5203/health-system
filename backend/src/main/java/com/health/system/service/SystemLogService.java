package com.health.system.service;

import com.health.system.entity.SystemLog;
import com.health.system.repository.SystemLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemLogService {

    @Autowired
    private SystemLogRepository systemLogRepository;

    public void saveLog(String operation, String operator) {
        SystemLog log = new SystemLog();
        log.setOperation(operation);
        log.setOperator(operator);
        systemLogRepository.save(log);
    }
}
