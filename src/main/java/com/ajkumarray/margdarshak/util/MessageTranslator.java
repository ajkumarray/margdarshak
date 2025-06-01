package com.ajkumarray.margdarshak.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class MessageTranslator {

    private static ResourceBundleMessageSource messageSource;

    @Autowired
    MessageTranslator(ResourceBundleMessageSource messageSource) {
        MessageTranslator.messageSource = messageSource;
    }

    public static String toLocale(String code) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(code, null, locale);
    }

    public static String toLocale(String lang, String code) {
        List<String> langList = Arrays.asList("en");

        if (!langList.contains(lang)) {
            lang = "en";
        }

        Locale locale = new Locale(lang);
        return messageSource.getMessage(code, null, locale);
    }
}
