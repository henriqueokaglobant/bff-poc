package com.globant.study.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globant.study.dto.ComponentDTO;
import com.globant.study.entity.LocalizationEntity;
import com.globant.study.entity.RuleEntity;
import com.globant.study.entity.ComponentEntity;
import com.globant.study.repository.LocalizationRepository;
import com.globant.study.repository.RuleRepository;
import com.globant.study.repository.ComponentRepository;
import com.globant.study.utils.Utils;
import org.apache.commons.lang3.BooleanUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private ComponentRepository componentRepository;

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

    //    @Cacheable(value = "calculateScreenJson")
    public List<ComponentDTO> calculateScreenJson(String template, String user) {
        String license = getLicenseFromUser(user);
        String role = getRoleFromUser(user);
        Locale locale = getLocaleFromUser(user);
        List<ComponentDTO> componentDTOList = jsonDataInFile ? readScreenDTOFromFile(template) : readScreenDTOFromDB(template);
        List<RuleEntity> ruleEntityList = new ArrayList<>();
        // Gather rules
        ruleEntityList.addAll(filterOutByPropertyRules(template, "license", license));
        ruleEntityList.addAll(filterOutByPropertyRules(template, "role", role));
        // Apply rules (sets some fields like 'include' and 'label' with calculated data)
        applyRules(componentDTOList, ruleEntityList, locale);
        LOGGER.info("\n" + formattedLogString(0, componentDTOList));
        // Return filtered data: only elements which are supposed to be included
        return filterComponents(componentDTOList);
    }

    private List<ComponentDTO> readScreenDTOFromFile(String template) {
        List<ComponentDTO> componentDTOList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, true);
        try (InputStream in = new FileInputStream(ResourceUtils.getFile("classpath:screen/" + template + ".json"))) {
            String fileContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            componentDTOList = objectMapper.readValue(fileContent, new TypeReference<List<ComponentDTO>>() {
            });
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
        // Adding the file/template because this is a field not present in the json
        componentDTOList.forEach(dto -> {
            dto.setTemplate(template);
        });
        return componentDTOList;
    }

    private List<ComponentDTO> readScreenDTOFromDB(String template) {
        ModelMapper modelMapper = new ModelMapper();
        List<ComponentEntity> componentEntityList = componentRepository.findByTemplate(template);
        return modelMapper.map(componentEntityList, new TypeToken<List<ComponentDTO>>() {
        }.getType());
    }

    private List<RuleEntity> filterOutByPropertyRules(String template, String propertyName, String propertyValue) {
        List<RuleEntity> ruleEntityList = ruleRepository.findByTemplateAndPropertyNameAndPropertyValue(template, propertyName, propertyValue);
        LOGGER.info(Utils.magenta("\nRules found: \n" + ruleEntityList.stream()
                .map(r -> r.getComponentName() + "/" + (BooleanUtils.isTrue(r.getInclude()) ? "show" : "hide")).collect(Collectors.joining("\n"))));
        return ruleEntityList;
    }

    private void applyRules(List<ComponentDTO> componentDTOList, List<RuleEntity> ruleEntityList, Locale locale) {
        componentDTOList.forEach(dto -> {
            boolean isExcludedByDefault = dto.getExcludeByDefault();
            boolean hasInclusionRule = ruleEntityList.stream().anyMatch(r -> dto.getName().equals(r.getComponentName()) && BooleanUtils.isTrue(r.getInclude()));
            boolean hasExclusionRule = ruleEntityList.stream().anyMatch(r -> dto.getName().equals(r.getComponentName()) && BooleanUtils.isFalse(r.getInclude()));
            Integer orderPriority = ruleEntityList.stream().filter(r -> dto.getName().equals(r.getComponentName())).findFirst().map(RuleEntity::getOrderPriority).orElse(null);
            // must be included either by default or by rules, rule should override default settings
            dto.setInclude(!(isExcludedByDefault || hasExclusionRule) || hasInclusionRule);
            dto.setOrderPriority(orderPriority);

            String label = localizationInFile ? messageSource.getMessage(dto.getLabelKey(), null, locale) : localizationRepository.findByLocaleAndMessageKey(locale.toString(), dto.getLabelKey()).map(LocalizationEntity::getMessageValue).orElse("");
            dto.setLabel(label.isBlank() ? dto.getLabelKey() : label);
            // Also applying rules to the child elements
            applyRules(dto.getOptions(), ruleEntityList, locale);
            applyRules(dto.getChildren(), ruleEntityList, locale);
        });
        componentDTOList.sort(Comparator.comparing(ComponentDTO::getOrderPriority, Comparator.nullsLast(Comparator.naturalOrder())));
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

    private String formattedLogString(int level, List<ComponentDTO> componentDTOList) {
        StringBuffer sb = new StringBuffer();
        componentDTOList.forEach(dto -> {
            sb.append("\t".repeat(level));
            sb.append(dto.getInclude() ? Utils.green(dto.toString()) : Utils.red(dto.toString())).append("\n");
            sb.append(formattedLogString(level + 1, dto.getChildren()));
            sb.append(formattedLogString(level + 1, dto.getOptions()));
        });
        return sb.toString();
    }

    /**
     * Return the list of components that has include=true, applied to all of the levels
     */
    private List<ComponentDTO> filterComponents(List<ComponentDTO> componentDTOList) {
        List<ComponentDTO> result = new ArrayList<>();
        result = componentDTOList.stream().filter(ComponentDTO::getInclude).toList();
        result.forEach(dto -> {
            dto.setOptions(filterComponents(dto.getOptions()));
            dto.setChildren(filterComponents(dto.getChildren()));
        });
        return result;
    }
}
