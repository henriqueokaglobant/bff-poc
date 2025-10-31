package com.globant.study.sql.repository;

import com.globant.study.sql.entity.ComponentEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SQLComponentRepository extends JpaRepository<ComponentEntity, Integer> {

    @Cacheable("components")
    List<ComponentEntity> findByTemplate(String template);
}
