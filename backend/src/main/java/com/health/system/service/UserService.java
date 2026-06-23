package com.health.system.service;

import com.health.system.entity.User;
import com.health.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Map<String, Object> register(User user) {

        Map<String, Object> result = new HashMap<>();

        // 判断用户名是否存在
        User existUser = userRepository.findByUsername(user.getUsername());
        if (existUser != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }

        // 保存用户
        User savedUser = userRepository.save(user);
        result.put("success", true);
        result.put("message", "注册成功");
        result.put("user", buildUserData(savedUser));
        return result;
    }

    public Map<String, Object> login(String username, String password) {

        Map<String, Object> result = new HashMap<>();

        User user = userRepository.findByUsername(username);

        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        if (!user.getPassword().equals(password)) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }

        result.put("success", true);
        result.put("message", "登录成功");
        result.put("user", buildUserData(user));
        return result;
    }

    private Map<String, Object> buildUserData(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("age", user.getAge());
        userData.put("gender", user.getGender());
        userData.put("phone", user.getPhone());
        return userData;
    }
}
