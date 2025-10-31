package com.globant.study.service;

import com.globant.study.sql.entity.LocalizationEntity;
import com.globant.study.sql.repository.SQLLocalizationRepository;
import com.globant.study.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class LocalizationService {
    public static final String LOCALIZATION_SQL = "SQL";
    public static final String LOCALIZATION_FILE = "FILE";
    public final Logger LOGGER = Logger.getLogger(getClass().getName());
    public final static String LOCALIZER_AFFIX = "@@"; // Avoid using special characters because regex is used in combination with these

    @Autowired
    private SQLLocalizationRepository SQLLocalizationRepository;

    @Autowired
    private MessageSource messageSource;

    @Value("${localizationData}")
    private String localizationData;

    /**
     * Localize the labelKey, it should have this pattern @@key@@ (also check affix variable to make sure it's right)
     * In case of not finding any matches, it will return the key as the value
     * Depending on the properties of the app, the localization can be done by using the messages.properties file or the db
     * <p>
     * This method needs to be a separate Spring Bean due to caching not working when calling inner methods of the same bean
     */
    @Cacheable(value = "localize")
    public String localize(String labelKey, Locale locale) {
        String message = "";

        LOGGER.info(Utils.yellow("LOCALIZING " + labelKey + "/" + locale));
        if (labelKey.startsWith(LOCALIZER_AFFIX) && labelKey.endsWith(LOCALIZER_AFFIX) && labelKey.length() >= 2 * LOCALIZER_AFFIX.length()) {
            labelKey = labelKey.substring(LOCALIZER_AFFIX.length(), labelKey.length() - LOCALIZER_AFFIX.length());
            if (localizationData.equals(LOCALIZATION_SQL)) {
                message = SQLLocalizationRepository.findByLocaleAndMessageKey(locale.toString(), labelKey).map(LocalizationEntity::getMessageValue).orElse("");
            } else if (localizationData.equals(LOCALIZATION_FILE)) {
                message = messageSource.getMessage(labelKey, null, locale);
            }
        }
        if (StringUtils.isEmpty(message)) {
            message = labelKey;
        }
        return message;
    }
}
