package com.globant.study.sql.repository;

import com.globant.study.sql.entity.RuleEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SQLRuleRepository extends JpaRepository<RuleEntity, Integer> {

    @Cacheable("rules")
    List<RuleEntity> findByTemplateAndPropertyNameAndPropertyValue(String template, String propertyName, String propertyValue);
}
