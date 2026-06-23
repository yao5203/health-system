package com.health.system.service;

import com.health.system.dto.AdminLoginRequest;
import com.health.system.entity.SystemAdmin;
import com.health.system.repository.SystemAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminService {

    @Autowired
    private SystemAdminRepository systemAdminRepository;

    @Autowired
    private SystemLogService systemLogService;

    public Map<String, Object> login(AdminLoginRequest request) {
        Map<String, Object> result = new HashMap<>();
        SystemAdmin admin = systemAdminRepository.findByUsername(request.getUsername());
        if (admin == null) {
            result.put("success", false);
            result.put("message", "系统管理员账号不存在");
            return result;
        }
        if (!admin.getPassword().equals(request.getPassword())) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }
        systemLogService.saveLog("系统管理员登录", admin.getUsername());
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("adminId", admin.getId());
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("displayName", admin.getDisplayName());
        return result;
    }
}
