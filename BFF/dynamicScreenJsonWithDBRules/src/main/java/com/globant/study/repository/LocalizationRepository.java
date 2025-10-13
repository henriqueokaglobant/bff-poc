package com.globant.study.repository;

import com.globant.study.entity.LocalizationEntity;
import com.globant.study.entity.ScreenComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocalizationRepository extends JpaRepository<LocalizationEntity, Integer> {

    LocalizationEntity findByLocaleAndMessageKey(String locale, String messageKey);
}
