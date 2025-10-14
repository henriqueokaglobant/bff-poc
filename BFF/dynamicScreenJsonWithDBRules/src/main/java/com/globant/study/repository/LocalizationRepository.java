package com.globant.study.repository;

import com.globant.study.entity.LocalizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocalizationRepository extends JpaRepository<LocalizationEntity, Integer> {

    Optional<LocalizationEntity> findByLocaleAndMessageKey(String locale, String messageKey);
}
