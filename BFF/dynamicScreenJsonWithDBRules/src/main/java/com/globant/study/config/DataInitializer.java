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
        boolean include = true;
        boolean exclude = false;
        ruleRepository.save(createRule("user_profile", "license", "enterprise", "company", include));
        ruleRepository.save(createRule("user_profile", "license", "free", "document", include));
        ruleRepository.save(createRule("user_profile", "license", "free", "licenseNumber", exclude));
        ruleRepository.save(createRule("user_profile", "role", "admin", "supportNumber", include));
        ruleRepository.save(createRule("user_profile", "role", "support", "isAdmin", include));
        ruleRepository.save(createRule("user_profile", "role", "support", "permissions", exclude));
        ruleRepository.save(createRule("customer_onboarding", "role", "support", "permissions", include));
        LOGGER.info(Utils.green("Initial data inserted into H2 database."));
    }

    private RuleEntity createRule(String template, String propertyName, String propertyValue, String jsonContent, boolean include) {
        RuleEntity rule = new RuleEntity();
        rule.setTemplate(template);
        rule.setPropertyName(propertyName);
        rule.setPropertyValue(propertyValue);
        rule.setJsonItem(jsonContent);
        rule.setInclude(include);
        return rule;
    }

}