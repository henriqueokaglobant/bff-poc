package com.globant.study.repository;

import com.globant.study.entity.RuleEntity;
import com.globant.study.entity.ScreenComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenComponentRepository extends JpaRepository<ScreenComponentEntity, Integer> {

    List<ScreenComponentEntity> findByTemplateName(String templateName);
}
