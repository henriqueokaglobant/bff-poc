package com.globant.study.config;

import com.globant.study.entity.RuleEntity;
import com.globant.study.repository.RuleRepository;
import com.globant.study.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {
    public final Logger LOGGER = Logger.getLogger(getClass().getName());

    @Autowired
    private RuleRepository ruleRepository;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        initializeData();
    }

    private void initializeData() {
        ruleRepository.save(createRule("license", "enterprise", "company"));
        ruleRepository.save(createRule("license", "free", "document"));
        ruleRepository.save(createRule("license", "free", "licenseNumber"));
        ruleRepository.save(createRule("role", "admin", "supportNumber"));
        ruleRepository.save(createRule("role", "support", "isAdmin"));
        ruleRepository.save(createRule("role", "support", "permissions"));
        LOGGER.info(Utils.green("======================================="));
        LOGGER.info(Utils.green("======================================="));
        LOGGER.info(Utils.green("======================================="));
        LOGGER.info(Utils.green("======================================="));
        LOGGER.info(Utils.green("Initial data inserted into H2 database."));
        LOGGER.info(Utils.green("======================================="));
        LOGGER.info(Utils.green("======================================="));
        LOGGER.info(Utils.green("======================================="));
        LOGGER.info(Utils.green("======================================="));
    }

    private RuleEntity createRule(String propertyName, String propertyValue, String jsonContent) {
        RuleEntity rule = new RuleEntity();
        rule.setPropertyName(propertyName);
        rule.setPropertyValue(propertyValue);
        rule.setJsonItem(jsonContent);
        return rule;
    }

}