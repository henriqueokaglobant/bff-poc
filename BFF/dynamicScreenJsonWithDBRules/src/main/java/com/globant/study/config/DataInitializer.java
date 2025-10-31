package com.globant.study.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globant.study.nosql.document.ComponentDocument;
import com.globant.study.nosql.repository.NoSQLComponentRepository;
import com.globant.study.sql.entity.ComponentEntity;
import com.globant.study.sql.entity.LocalizationEntity;
import com.globant.study.sql.entity.RuleEntity;
import com.globant.study.sql.repository.SQLComponentRepository;
import com.globant.study.sql.repository.SQLLocalizationRepository;
import com.globant.study.sql.repository.SQLRuleRepository;
import com.globant.study.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {
    public final Logger LOGGER = Logger.getLogger(getClass().getName());

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private SQLRuleRepository SQLRuleRepository;

    @Autowired
    private SQLLocalizationRepository SQLLocalizationRepository;

    @Autowired
    private SQLComponentRepository SQLComponentRepository;

    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Autowired
    private NoSQLComponentRepository noSQLComponentRepository;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        LOGGER.info(Utils.green("======Starting application data initializer========"));
        initializeRuleData(); // Create mocked rules in the DB
        initializeJsonComponentData(); // Read json files and save on the DB
        initializeLocalizationData(); // Read messages files and save on the DB
        LOGGER.info(Utils.green("======Finishing application data initializer========"));
    }

    private void initializeRuleData() {
        boolean include = true;
        boolean exclude = false;

        SQLRuleRepository.save(createRule("user_profile", "license", "enterprise", "company.document", include, null));
        SQLRuleRepository.save(createRule("user_profile", "license", "enterprise", "person.document", exclude, null));
        SQLRuleRepository.save(createRule("user_profile", "license", "enterprise", "person.address", exclude, null));
        SQLRuleRepository.save(createRule("user_profile", "license", "enterprise", "person.address.zipcode", exclude, null));

        SQLRuleRepository.save(createRule("user_profile", "role", "admin", "company.supportNumber", include, null));
        SQLRuleRepository.save(createRule("user_profile", "role", "admin", "user.isAdmin", include, 2));
        SQLRuleRepository.save(createRule("user_profile", "role", "admin", "user.permissions", include, 2));
        SQLRuleRepository.save(createRule("user_profile", "role", "admin", "company.type.options.contractor", null, 13));
        SQLRuleRepository.save(createRule("user_profile", "role", "admin", "company.type.options.client", null, 12));
        SQLRuleRepository.save(createRule("user_profile", "role", "admin", "company.type.options.partner", null, 11));
        SQLRuleRepository.save(createRule("user_profile", "role", "admin", "user", null, 1));


        SQLRuleRepository.save(createRule("user_profile", "role", "support", "company.type.options.contractor", exclude, null));

        SQLRuleRepository.save(createRule("user_profile", "license", "free", "user.document", include, null));
        SQLRuleRepository.save(createRule("user_profile", "license", "free", "company.licenseNumber", exclude, null));

        SQLRuleRepository.save(createRule("user_profile", "role", "support", "user.isAdmin", include, null));
        SQLRuleRepository.save(createRule("user_profile", "role", "support", "user.permissions", exclude, null));

        SQLRuleRepository.save(createRule("customer_onboarding", "role", "support", "user.permissions", include, null));
        LOGGER.info(Utils.green("Initial RULE data inserted into the database."));
    }

    private void initializeJsonComponentData() {
        try {
            List<ComponentEntity> componentEntityList = readComponentEntityFromFile();
            SQLComponentRepository.saveAll(componentEntityList);
            List<ComponentDocument> componentDocumentList = readComponentDocumentFromFile();
            noSQLComponentRepository.deleteAll(); // Clear if already has data
            noSQLComponentRepository.saveAll(componentDocumentList);
            LOGGER.info(Utils.green("Initial JSON COMPONENT data inserted into the database."));
        } catch (IOException e) {
            LOGGER.info(Utils.red("Failed to initialize JSON COMPONENT data."));
        }
    }

    private List<ComponentEntity> readComponentEntityFromFile() throws IOException {
        List<ComponentEntity> componentList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, true);
        for (String resourceFile : getResourceFiles("classpath:screen/*.json")) {
            try (InputStream in = resourceLoader.getResource("classpath:screen/" + resourceFile).getInputStream()) {
                String fileContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                List<ComponentEntity> componentListForTemplate = objectMapper.readValue(fileContent, new TypeReference<List<ComponentEntity>>() {
                });
                componentListForTemplate.forEach(dto -> {
                    dto.setTemplate(resourceFile.replace(".json", ""));
                });
                componentList.addAll(componentListForTemplate);
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
            }
        }
        fillParentsEntity(componentList);
        return componentList;
    }

    private List<ComponentDocument> readComponentDocumentFromFile() throws IOException {
        List<ComponentDocument> componentList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, true);
        for (String resourceFile : getResourceFiles("classpath:screen/*.json")) {
            try (InputStream in = resourceLoader.getResource("classpath:screen/" + resourceFile).getInputStream()) {
                String fileContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                List<ComponentDocument> componentListForTemplate = objectMapper.readValue(fileContent, new TypeReference<List<ComponentDocument>>() {
                });
                componentListForTemplate.forEach(dto -> {
                    dto.setTemplate(resourceFile.replace(".json", ""));
                });
                componentList.addAll(componentListForTemplate);
            } catch (IOException e) {
                LOGGER.severe(e.getMessage());
            }
        }
        return componentList;
    }

    public void fillParentsEntity(List<ComponentEntity> componentList) {
        componentList.forEach(item -> {
            item.getOptions().forEach(opt -> opt.setOptionParentComponent(item));
            item.getChildren().forEach(opt -> opt.setParentComponent(item));
            fillParentsEntity(item.getOptions());
            fillParentsEntity(item.getChildren());
        });
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

    private RuleEntity createRule(String template, String propertyName, String propertyValue, String jsonContent, Boolean include, Integer orderPriority) {
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
        try {
            for (Locale locale : findAvailableLocales()) {
                localizationEntityList.addAll(getAllMessagesForLocale(locale));
            }
            SQLLocalizationRepository.saveAll(localizationEntityList);
            LOGGER.info(Utils.green("Initial Localization data (messages) inserted into the database."));
        } catch (IOException e) {
            LOGGER.info(Utils.red("Failed to initialize LOCALIZATION data."));
        }
    }

    public List<Locale> findAvailableLocales() throws IOException {
        List<Locale> locales = new ArrayList<>();
        String bundleName = "messages";

        // This is the regex pattern for finding locales.
        // It captures the language and country codes from filenames like messages_en_US.properties
        Pattern pattern = Pattern.compile("^" + bundleName + "(?:_([a-z]{2})(?:_([A-Z]{2}))?)?\\.properties$");

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> urls = classLoader.getResources("");

        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            // In a real application, you'd need more sophisticated logic
            // to handle different resource types (e.g., jar files).
            java.io.File directory = new java.io.File(url.getPath());
            if (directory.exists() && directory.isDirectory()) {
                for (java.io.File file : Objects.requireNonNull(directory.listFiles())) {
                    Matcher matcher = pattern.matcher(file.getName());
                    if (matcher.matches()) {
                        String language = matcher.group(1);
                        String country = matcher.group(2);
                        if (language != null && country != null) {
                            locales.add(new Locale(language, country));
                        } else if (language != null) {
                            locales.add(new Locale(language));
                        }
                    }
                }
            }
        }
        // Add the default locale, which corresponds to `messages.properties`
        locales.add(Locale.ROOT);
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