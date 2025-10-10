package com.globant.study.repository;

import com.globant.study.entity.RuleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleRepository extends JpaRepository<RuleEntity, Integer> {

    List<RuleEntity> findByTemplateAndPropertyNameAndPropertyValue(String template, String propertyName, String propertyValue);
}
