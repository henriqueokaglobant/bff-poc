package com.globant.study.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globant.study.dto.ComponentDTO;
import com.globant.study.entity.ComponentEntity;
import com.globant.study.entity.LocalizationEntity;
import com.globant.study.entity.RuleEntity;
import com.globant.study.repository.ComponentRepository;
import com.globant.study.repository.LocalizationRepository;
import com.globant.study.repository.RuleRepository;
import com.globant.study.utils.Utils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LocalizationService {
    public final Logger LOGGER = Logger.getLogger(getClass().getName());
    public final static String LOCALIZER_AFFIX = "@@"; // Avoid using special characters because regex is used in combination with these

    @Autowired
    private LocalizationRepository localizationRepository;

    @Autowired
    private MessageSource messageSource;

    @Value("#{${localizationInFile}}")
    private Boolean localizationInFile;

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
}
