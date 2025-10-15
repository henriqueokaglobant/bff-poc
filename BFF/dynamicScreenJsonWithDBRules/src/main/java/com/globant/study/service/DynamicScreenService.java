package com.globant.study.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globant.study.dto.ScreenComponentDTO;
import com.globant.study.entity.LocalizationEntity;
import com.globant.study.entity.RuleEntity;
import com.globant.study.entity.ScreenComponentEntity;
import com.globant.study.repository.LocalizationRepository;
import com.globant.study.repository.RuleRepository;
import com.globant.study.repository.ScreenComponentRepository;
import com.globant.study.utils.Utils;
import org.apache.commons.lang3.BooleanUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class DynamicScreenService {
    public final Logger LOGGER = Logger.getLogger(getClass().getName());
    public final Comparator<Integer> naturalOrderNullsLast = Comparator.nullsLast(Comparator.naturalOrder());

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private ScreenComponentRepository screenComponentRepository;

    @Autowired
    private LocalizationRepository localizationRepository;

    @Autowired
    private MessageSource messageSource;

    @Value("#{${license}}")
    private Map<String, String> licenseByUser;

    @Value("#{${role}}")
    private Map<String, String> roleByUser;

    @Value("#{${country}}")
    private Map<String, String> countryByUser;

    @Value("#{${language}}")
    private Map<String, String> languageByUser;

    @Value("#{${jsonDataInFile}}")
    private Boolean jsonDataInFile;

    @Value("#{${localizationInFile}}")
    private Boolean localizationInFile;

    @Cacheable(value = "calculateScreenJson")
    public List<ScreenComponentDTO> calculateScreenJson(String template, String user) {
        String license = getLicenseFromUser(user);
        String role = getRoleFromUser(user);
        Locale locale = getLocaleFromUser(user);
        List<ScreenComponentDTO> screenComponentDTOList = jsonDataInFile ? readScreenDTOFromFile(template) : readScreenDTOFromDB(template);
        List<RuleEntity> ruleEntityList = new ArrayList<>();
        // Gather rules
        ruleEntityList.addAll(filterOutByPropertyRules(template, "license", license));
        ruleEntityList.addAll(filterOutByPropertyRules(template, "role", role));
        // Apply rules (basically it's setting the include flag)
        applyRules(screenComponentDTOList, ruleEntityList, locale);
        logResultData(screenComponentDTOList);
        // Return filtered data: only elements which are supposed to be included
        return screenComponentDTOList.stream().filter(ScreenComponentDTO::getInclude).toList();
    }

    private List<ScreenComponentDTO> readScreenDTOFromFile(String template) {
        List<ScreenComponentDTO> screenComponentDTOList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, true);
        try (InputStream in = new FileInputStream(ResourceUtils.getFile("classpath:screen/" + template + ".json"))) {
            String fileContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            screenComponentDTOList = objectMapper.readValue(fileContent, new TypeReference<List<ScreenComponentDTO>>() {
            });
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        screenComponentDTOList.forEach(dto -> {
            dto.setTemplate(template);
        });
        return screenComponentDTOList;
    }

    private List<ScreenComponentDTO> readScreenDTOFromDB(String template) {
        ModelMapper modelMapper = new ModelMapper();
        List<ScreenComponentEntity> screenComponentEntityList = screenComponentRepository.findByTemplate(template);
        return modelMapper.map(screenComponentEntityList, new TypeToken<List<ScreenComponentDTO>>() {
        }.getType());
    }

    private List<RuleEntity> filterOutByPropertyRules(String template, String propertyName, String propertyValue) {
        List<RuleEntity> ruleEntityList = ruleRepository.findByTemplateAndPropertyNameAndPropertyValue(template, propertyName, propertyValue);
        LOGGER.info(Utils.magenta("\nRules found: \n" + ruleEntityList.stream()
                .map(r -> r.getComponentName() + "/" + (BooleanUtils.isTrue(r.getInclude()) ? "show" : "hide")).collect(Collectors.joining("\n"))));
        return ruleEntityList;
    }

    private void applyRules(List<ScreenComponentDTO> screenComponentDTOList, List<RuleEntity> ruleEntityList, Locale locale) {
        screenComponentDTOList.forEach(dto -> {
            boolean isIncludedByDefault = dto.getIncludeByDefault();
            boolean hasInclusionRule = ruleEntityList.stream().anyMatch(r -> dto.getName().equals(r.getComponentName()) && r.getInclude());
            boolean hasExclusionRule = ruleEntityList.stream().anyMatch(r -> dto.getName().equals(r.getComponentName()) && !r.getInclude());
            Integer orderPriority =   ruleEntityList.stream().filter(r -> dto.getName().equals(r.getComponentName())).findFirst().map(RuleEntity::getOrderPriority).orElse(null);
            // must be included either by default or by rules, exclusion will override any kind of inclusion
            dto.setInclude((isIncludedByDefault || hasInclusionRule) && !hasExclusionRule);
            dto.setOrderPriority(orderPriority);

            String label = localizationInFile ? messageSource.getMessage(dto.getLabelKey(), null, locale) : localizationRepository.findByLocaleAndMessageKey(locale.toString(), dto.getLabelKey()).map(LocalizationEntity::getMessageValue).orElse("");
            dto.setLabel(label);
        });
        screenComponentDTOList.sort(Comparator.comparing(ScreenComponentDTO::getOrderPriority, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    private String getLicenseFromUser(String user) {
        String result = licenseByUser.get(user);
        LOGGER.info(Utils.blue("\nLicense found: " + result));
        return result;
    }

    private String getRoleFromUser(String user) {
        String result = roleByUser.get(user);
        LOGGER.info(Utils.blue("\nRole found: " + result));
        return result;
    }

    private Locale getLocaleFromUser(String user) {
        String country = countryByUser.get(user);
        String language = languageByUser.get(user);
        Locale locale = new Locale.Builder().setLanguage(language).setRegion(country).build();
        LOGGER.info(Utils.blue("\nLocale found: " + locale));
        return locale;
    }

    private void logResultData(List<ScreenComponentDTO> screenComponentDTOList) {
        StringBuffer logResult = new StringBuffer("\n=====FILTERED LIST:=====\n");
        screenComponentDTOList.forEach(dto -> {
            if (dto.getInclude()) {
                logResult.append(Utils.green(dto.toString())).append("\n");
            } else {
                logResult.append(Utils.red(dto.toString())).append("\n");
            }
        });
        LOGGER.info(logResult.toString());
    }
}
