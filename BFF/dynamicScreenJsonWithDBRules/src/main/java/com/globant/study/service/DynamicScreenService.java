package com.globant.study.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globant.study.dto.ComponentDTO;
import com.globant.study.nosql.document.ComponentDocument;
import com.globant.study.nosql.repository.NoSQLComponentRepository;
import com.globant.study.sql.entity.ComponentEntity;
import com.globant.study.sql.entity.RuleEntity;
import com.globant.study.sql.repository.SQLComponentRepository;
import com.globant.study.sql.repository.SQLRuleRepository;
import com.globant.study.utils.Utils;
import org.apache.commons.lang3.BooleanUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DynamicScreenService {
    public static final String REGEX_NEAREST_ALL_CHARS = ".*?";
    public final Logger LOGGER = Logger.getLogger(getClass().getName());
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final String JSON_DATA_SQL = "SQL";
    public static final String JSON_DATA_NOSQL = "NOSQL";
    public static final String JSON_DATA_FILE_SQL = "FILE";

    @Autowired
    LocalizationService localizationService;

    @Autowired
    SQLRuleRepository SQLRuleRepository;

    @Autowired
    SQLComponentRepository SQLComponentRepository;

    @Autowired
    NoSQLComponentRepository noSQLComponentRepository;

    @Value("#{${license}}")
    Map<String, String> licenseByUser;

    @Value("#{${role}}")
    Map<String, String> roleByUser;

    @Value("#{${country}}")
    Map<String, String> countryByUser;

    @Value("#{${language}}")
    Map<String, String> languageByUser;

    @Value("${jsonData}")
    String jsonData;

    @Cacheable(value = "calculateScreenJson")
    public List<ComponentDTO> calculateScreenJson(String template, String user) {
        String license = getLicenseFromUser(user);
        String role = getRoleFromUser(user);
        Locale locale = getLocaleFromUser(user);
        List<ComponentDTO> componentDTOList = new ArrayList<>();
        if (jsonData.equals(JSON_DATA_SQL)) {
            componentDTOList = readScreenDTOFromSQLDB(template);
        } else if (jsonData.equals(JSON_DATA_NOSQL)) {
            componentDTOList = readScreenDTOFromNOSQLDB(template);
        } else if (jsonData.equals(JSON_DATA_FILE_SQL)) {

            componentDTOList = readScreenDTOFromFile(template);
        }
        List<RuleEntity> ruleEntityList = new ArrayList<>();
        // Gather rules
        ruleEntityList.addAll(filterOutByPropertyRules(template, "license", license));
        ruleEntityList.addAll(filterOutByPropertyRules(template, "role", role));
        // Apply rules
        applyRules(componentDTOList, ruleEntityList, locale);
        LOGGER.info("\n" + formattedLogString(0, componentDTOList));
        // Filter out hidden fields
        return filterComponents(componentDTOList);
    }

    private List<ComponentDTO> readScreenDTOFromFile(String template) {
        List<ComponentDTO> componentDTOList = new ArrayList<>();
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

    private List<ComponentDTO> readScreenDTOFromSQLDB(String template) {
        ModelMapper modelMapper = new ModelMapper();
        List<ComponentEntity> componentEntityList = SQLComponentRepository.findByTemplate(template);
        return modelMapper.map(componentEntityList, new TypeToken<List<ComponentDTO>>() {
        }.getType());
    }

    private List<ComponentDTO> readScreenDTOFromNOSQLDB(String template) {
        ModelMapper modelMapper = new ModelMapper();
        List<ComponentDocument> componentEntityList = noSQLComponentRepository.findByTemplate(template);
        return modelMapper.map(componentEntityList, new TypeToken<List<ComponentDTO>>() {
        }.getType());
    }

    private List<RuleEntity> filterOutByPropertyRules(String template, String propertyName, String propertyValue) {
        List<RuleEntity> ruleEntityList = SQLRuleRepository.findByTemplateAndPropertyNameAndPropertyValue(template, propertyName, propertyValue);
        LOGGER.info(Utils.magenta("\nRules found: \n" + ruleEntityList.stream()
                .map(r -> r.getComponentName() + "/" + (BooleanUtils.isTrue(r.getInclude()) ? "show" : "hide")).collect(Collectors.joining("\n"))));
        return ruleEntityList;
    }

    /**
     * Will apply these 3 rules to all elements in all levels: Ordering, Localization and Show/Hide
     */
    private void applyRules(List<ComponentDTO> componentDTOList, List<RuleEntity> ruleEntityList, Locale locale) {
        componentDTOList.forEach(dto -> {
            boolean isExcludedByDefault = dto.getExcludeByDefault();
            boolean hasInclusionRule = ruleEntityList.stream().anyMatch(r -> dto.getName().equals(r.getComponentName()) && BooleanUtils.isTrue(r.getInclude()));
            boolean hasExclusionRule = ruleEntityList.stream().anyMatch(r -> dto.getName().equals(r.getComponentName()) && BooleanUtils.isFalse(r.getInclude()));
            Integer orderPriority = ruleEntityList.stream().filter(r -> dto.getName().equals(r.getComponentName())).findFirst().map(RuleEntity::getOrderPriority).orElse(null);
            // must be included either by default or by rules, rule should override default settings
            dto.setInclude(!(isExcludedByDefault || hasExclusionRule) || hasInclusionRule);
            // avoid calculating ordering and localization for components that will not be included
            if (dto.getInclude()) {
                dto.setOrderPriority(orderPriority);

                // Localize Title if present
                if (dto.getTitleKey() != null) {
                    dto.setTitle(localizationService.localize(dto.getTitleKey(), locale));
                }
                // Also applying rules to the child elements
                applyRules(dto.getOptions(), ruleEntityList, locale);
                applyRules(dto.getChildren(), ruleEntityList, locale);

                // Localize generic properties
                dto.setProperties(localizeGenericProperties(dto.getProperties(), locale));
            }
        });
        componentDTOList.sort(Comparator.comparing(ComponentDTO::getOrderPriority, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    public Map<String, Object> localizeGenericProperties(Map<String, Object> properties, Locale locale) {
        Map<String, Object> localizedProperties = properties;
        try {
            String jsonString = objectMapper.writeValueAsString(properties);
            String localizedJsonString = jsonString;
            Pattern pattern = Pattern.compile(LocalizationService.LOCALIZER_AFFIX + REGEX_NEAREST_ALL_CHARS + LocalizationService.LOCALIZER_AFFIX);
            Matcher matcher = pattern.matcher(jsonString);
            for (MatchResult i : matcher.results().toList()) {
                localizedJsonString = localizedJsonString.replaceFirst(i.group(), localizationService.localize(i.group(), locale));
            }
            localizedProperties = objectMapper.readValue(localizedJsonString, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            LOGGER.severe("Error localizing generic properties fields");
        }
        return localizedProperties;
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
