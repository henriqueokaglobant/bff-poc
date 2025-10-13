package com.globant.study.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class Config {
    public final Logger LOGGER = Logger.getLogger(getClass().getName());

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames("messages"); // Path to your message files
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }


}