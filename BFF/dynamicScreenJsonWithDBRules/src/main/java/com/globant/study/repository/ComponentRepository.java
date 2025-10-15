package com.globant.study.repository;

import com.globant.study.entity.ComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComponentRepository extends JpaRepository<ComponentEntity, Integer> {

    List<ComponentEntity> findByTemplate(String template);
}
