package com.globant.study.nosql.repository;

import com.globant.study.nosql.document.ComponentDocument;
import com.globant.study.sql.entity.ComponentEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NoSQLComponentRepository extends MongoRepository<ComponentDocument, String> {
    @Cacheable("components")
    List<ComponentDocument> findByTemplate(String template);
}
