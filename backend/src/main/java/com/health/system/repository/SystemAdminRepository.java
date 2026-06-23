package com.health.system.repository;

import com.health.system.entity.SystemAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemAdminRepository extends JpaRepository<SystemAdmin, Long> {

    SystemAdmin findByUsername(String username);
}
