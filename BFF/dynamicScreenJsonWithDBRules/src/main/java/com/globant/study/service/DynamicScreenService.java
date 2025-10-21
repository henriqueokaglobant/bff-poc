package com.globant.study.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.apache.commons.lang3.StringUtils;
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
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DynamicScreenService {
    public final Logger LOGGER = Logger.getLogger(getClass().getName());
    public final Comparator<Integer> naturalOrderNullsLast = Comparator.nullsLast(Comparator.naturalOrder());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String LOCALIZER_AFFIX = "@@"; // Avoid using special characters because regex is used in combination with these

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

            if (dto.getTitleKey() != null) {
                dto.setTitle(localize(dto.getTitleKey(), locale));
            }
            // Also applying rules to the child elements
            applyRules(dto.getOptions(), ruleEntityList, locale);
            applyRules(dto.getChildren(), ruleEntityList, locale);
            dto.setProperties(localizeGenericProperties(dto.getProperties(), locale));
        });
        componentDTOList.sort(Comparator.comparing(ComponentDTO::getOrderPriority, Comparator.nullsLast(Comparator.naturalOrder())));
    }

    /**
     * Localize the labelKey, it should have this pattern @@key@@ (also check affix variable to make sure it's right)
     * In case of not finding any matches, it will return the key as the value
     * Depending on the properties of the app, the localization can be done by using the messages.properties file or the db
     */
    private String localize(String labelKey, Locale locale) {
        String message = "";
        if (labelKey.startsWith(LOCALIZER_AFFIX) && labelKey.endsWith(LOCALIZER_AFFIX) && labelKey.length() >= 2 * LOCALIZER_AFFIX.length()) {
            labelKey = labelKey.substring(LOCALIZER_AFFIX.length(), labelKey.length() - LOCALIZER_AFFIX.length());
            if (localizationInFile) {
                message = messageSource.getMessage(labelKey, null, locale);
            } else {
                message = localizationRepository.findByLocaleAndMessageKey(locale.toString(), labelKey).map(LocalizationEntity::getMessageValue).orElse("");
            }
        }
        if (StringUtils.isEmpty(message)) {
            message = labelKey;
        }
        return message;
    }

    public Map<String, Object> localizeGenericProperties(Map<String, Object> properties, Locale locale) {
        Map<String, Object> localizedProperties = properties;
        try {
            String jsonString = objectMapper.writeValueAsString(properties);
            String localizedJsonString = jsonString;
            Pattern pattern = Pattern.compile(LOCALIZER_AFFIX + ".*?" + LOCALIZER_AFFIX);
            Matcher matcher = pattern.matcher(jsonString);
            for (MatchResult i : matcher.results().toList()) {
                localizedJsonString = localizedJsonString.replaceFirst(i.group(), localize(i.group(), locale));
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
