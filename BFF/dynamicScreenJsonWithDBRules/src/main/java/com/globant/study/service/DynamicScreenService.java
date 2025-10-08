package com.globant.study.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globant.study.dto.ScreenDTO;
import com.globant.study.entity.RuleEntity;
import com.globant.study.repository.RuleRepository;
import com.globant.study.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class DynamicScreenService {
    public final Logger LOGGER = Logger.getLogger(getClass().getName());
    @Autowired
    private RuleRepository ruleRepository;

    public List<ScreenDTO> calculateScreenJson(String template, String license, String role) {
        List<ScreenDTO> screenDTOList = readScreenDTOFromFile(template);
        LOGGER.info(Utils.yellow("FULL LIST: \n" + screenDTOList.stream().map(ScreenDTO::toString).collect(Collectors.joining("\n"))));
        screenDTOList = filterOutByPropertyRules("license", license, screenDTOList);
        screenDTOList = filterOutByPropertyRules("role", role, screenDTOList);
        LOGGER.info(Utils.blue("FILTERED LIST: \n" + screenDTOList.stream().map(ScreenDTO::toString).collect(Collectors.joining("\n"))));

//        return screenDTOList.stream().map(ScreenDTO::toString).collect(Collectors.joining(", "));
        return screenDTOList;
    }

    private List<ScreenDTO> readScreenDTOFromFile(String template) {
        List<ScreenDTO> screenDTOList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, true);
        try (InputStream in = new FileInputStream(ResourceUtils.getFile("classpath:screen/" + template + ".json"))) {
            String fileContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            screenDTOList = objectMapper.readValue(fileContent, new TypeReference<List<ScreenDTO>>() {
            });
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        return screenDTOList;
    }

    private List<ScreenDTO> filterOutByPropertyRules(String propertyName, String propertyValue, List<ScreenDTO> screenDTOList) {
        List<RuleEntity> ruleEntityList = ruleRepository.findByPropertyNameAndPropertyValue(propertyName, propertyValue);
        List<String> toRemove = ruleEntityList.stream().map(RuleEntity::getJsonItem).toList();
        LOGGER.info(Utils.magenta("Removing: " + String.join(",", toRemove)));
        return screenDTOList.stream().filter(dto -> !toRemove.contains(dto.getFieldName())).toList();
    }
}
