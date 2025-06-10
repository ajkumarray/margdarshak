package com.ajkumarray.margdarshak.util;

import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class CommonFunctionHelper {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final int PATTERN_SIZE = 63;
    private static final String PATTERN = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private SecureRandom random = new SecureRandom();

    /**
     * Generates a random alpha numeric code of the given size.
     * 
     * @param size
     * @return
     */
    public String generateAlphaNumericCode(int size) {
        StringBuilder sb = new StringBuilder(size - 1);
        sb.append("MD");

        for (int i = 0; i < size - 1; i++) {
            int rand = random.nextInt(PATTERN_SIZE - 1);
            sb.append(PATTERN.charAt(rand));
        }

        return sb.toString();
    }

    public void commonLoggerHelper(Throwable exception, String identifier) {
        String loggerMessage = "Url-Exception-Handler-: " + identifier;

        log.error(loggerMessage);
        log.error(exception.getMessage());
    }

    public boolean isEmptyOrBlank(Object obj) {
        if (ObjectUtils.isEmpty(obj)) {
            return true;
        }
        if (obj instanceof String string) {
            return string.isBlank();
        }
        if (obj instanceof JsonNode jsonNode) {
            return jsonNode.isNull();
        }
        return false;
    }
}
