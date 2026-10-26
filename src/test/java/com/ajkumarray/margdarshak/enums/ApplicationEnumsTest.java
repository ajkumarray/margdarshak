package com.ajkumarray.margdarshak.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.jupiter.api.Test;

class ApplicationEnumsTest {

    @Test
    void everyCodeHasAMessage() {
        ResourceBundle bundle = ResourceBundle.getBundle("lang/messages", Locale.ENGLISH);

        assertThat(Arrays.stream(ApplicationEnums.values()).map(ApplicationEnums::getCode))
                .allMatch(bundle::containsKey);
    }

    @Test
    void successAndFailureCodesMapToMatchingMessages() {
        ResourceBundle bundle = ResourceBundle.getBundle("lang/messages", Locale.ENGLISH);

        assertThat(bundle.getString(ApplicationEnums.SUCCESS_MESSAGE.getCode())).isEqualTo("Success!");
        assertThat(bundle.getString(ApplicationEnums.FAILED_MESSAGE.getCode())).isEqualTo("Failed!");
        assertThat(bundle.getString(ApplicationEnums.URL_CREATION_SUCCESS.getCode()))
                .isEqualTo("URL creation success!");
    }
}
