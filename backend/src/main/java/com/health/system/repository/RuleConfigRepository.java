package com.health.system.repository;

import com.health.system.entity.RuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleConfigRepository extends JpaRepository<RuleConfig, Long> {

    RuleConfig findByRuleKey(String ruleKey);

    List<RuleConfig> findAllByOrderByRuleKeyAsc();
}
