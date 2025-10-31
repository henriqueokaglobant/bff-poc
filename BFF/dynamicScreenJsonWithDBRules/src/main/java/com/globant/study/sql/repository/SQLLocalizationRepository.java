package com.globant.study.sql.repository;

import com.globant.study.sql.entity.LocalizationEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SQLLocalizationRepository extends JpaRepository<LocalizationEntity, Integer> {

    @Cacheable("locale")
    Optional<LocalizationEntity> findByLocaleAndMessageKey(String locale, String messageKey);
}
