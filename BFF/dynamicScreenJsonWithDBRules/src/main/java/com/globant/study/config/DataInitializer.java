package com.globant.study.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globant.study.entity.LocalizationEntity;
import com.globant.study.entity.RuleEntity;
import com.globant.study.entity.ScreenComponentEntity;
import com.globant.study.repository.LocalizationRepository;
import com.globant.study.repository.RuleRepository;
import com.globant.study.repository.ScreenComponentRepository;
import com.globant.study.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

@Component
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {
    public final Logger LOGGER = Logger.getLogger(getClass().getName());

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private LocalizationRepository localizationRepository;

    @Autowired
    private ScreenComponentRepository screenComponentRepository;

    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        initializeRuleData(); // Create mocked rules in the DB
        initializeJsonComponentData(); // Read json files and save on the DB
        initializeLocalizationData(); // Read messages files and save on the DB
    }

    private void initializeRuleData() {
        boolean include = true;
        boolean exclude = false;
        ruleRepository.save(createRule("user_profile", "license", "enterprise", "company.document", include, 1));
        ruleRepository.save(createRule("user_profile", "license", "free", "user.document", include, null));
        ruleRepository.save(createRule("user_profile", "license", "free", "company.licenseNumber", exclude, null));
        ruleRepository.save(createRule("user_profile", "role", "admin", "company.supportNumber", include, null));
        ruleRepository.save(createRule("user_profile", "role", "support", "user.isAdmin", include, null));
        ruleRepository.save(createRule("user_profile", "role", "support", "user.permissions", exclude, null));
        ruleRepository.save(createRule("customer_onboarding", "role", "support", "user.permissions", include, null));
        LOGGER.info(Utils.green("Initial RULE data inserted into H2 database."));
    }

    private void initializeJsonComponentData() {
        try {
            List<ScreenComponentEntity> screenComponentEntityList = readScreenComponentFromFile();
            screenComponentRepository.saveAll(screenComponentEntityList);
            LOGGER.info(Utils.green("Initial JSON COMPONENT data inserted into H2 database."));
        } catch (IOException e) {
            LOGGER.info(Utils.red("Failed to initialize JSON COMPONENT data."));
        }
    }

    private List<ScreenComponentEntity> readScreenComponentFromFile() throws IOException {
        List<ScreenComponentEntity> screenComponentDTOList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, true);
        for (String resourceFile : getResourceFiles("classpath:screen/*.json")) {
            try (InputStream in = new FileInputStream(ResourceUtils.getFile("classpath:screen/" + resourceFile))) {
                String fileContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                List<ScreenComponentEntity> screenComponentEntityListForTemplate = objectMapper.readValue(fileContent, new TypeReference<List<ScreenComponentEntity>>() {
                });
                screenComponentEntityListForTemplate.forEach(dto -> {
                    dto.setTemplate(resourceFile.replace(".json", ""));
                });
                screenComponentDTOList.addAll(screenComponentEntityListForTemplate);
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
            }
        }
        return screenComponentDTOList;
    }

    public List<String> getResourceFiles(String locationPattern) throws IOException {
        List<String> fileNames = new ArrayList<>();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(locationPattern);

        for (Resource resource : resources) {
            fileNames.add(resource.getFilename());
        }
        return fileNames;
    }

    private RuleEntity createRule(String template, String propertyName, String propertyValue, String jsonContent, boolean include, Integer orderPriority) {
        RuleEntity rule = new RuleEntity();
        rule.setTemplate(template);
        rule.setPropertyName(propertyName);
        rule.setPropertyValue(propertyValue);
        rule.setComponentName(jsonContent);
        rule.setInclude(include);
        rule.setOrderPriority(orderPriority);
        return rule;
    }

    private void initializeLocalizationData() {
        List<LocalizationEntity> localizationEntityList = new ArrayList<>();
        for (Locale locale : getAllAvailableLocales()) {
            localizationEntityList.addAll(getAllMessagesForLocale(locale));
        }
        localizationRepository.saveAll(localizationEntityList);
        LOGGER.info(Utils.green("Initial Localization data (messages) inserted into H2 database."));
    }

    private Set<Locale> getAllAvailableLocales() {
        Set<Locale> locales = new HashSet<>();
        locales.add(new Locale.Builder().setLanguage("en").setRegion("US").build());
        locales.add(new Locale.Builder().setLanguage("pt").setRegion("BR").build());
        return locales;
    }

    private List<LocalizationEntity> getAllMessagesForLocale(Locale locale) {
        List<LocalizationEntity> localizationEntityList = new ArrayList<>();
        // Assuming your base name is "messages"
        ResourceBundle bundle = ResourceBundle.getBundle("messages", locale);
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            LocalizationEntity localizationEntity = new LocalizationEntity();
            String key = keys.nextElement();
            localizationEntity.setLocale(locale.toString());
            localizationEntity.setMessageKey(key);
            localizationEntity.setMessageValue(messageSource.getMessage(key, null, locale));
            localizationEntityList.add(localizationEntity);
        }
        return localizationEntityList;
    }

}