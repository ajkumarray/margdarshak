package com.ajkumarray.margdarshak.implementation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ajkumarray.margdarshak.entity.UrlMasterEntity;
import com.ajkumarray.margdarshak.enums.UrlStatusEnums;
import com.ajkumarray.margdarshak.repository.UrlRepository;
import com.ajkumarray.margdarshak.util.CommonFunctionHelper;
import com.ajkumarray.margdarshak.util.UrlHelper;

@ExtendWith(MockitoExtension.class)
class UrlImplementationTest {

    @Mock
    private CommonFunctionHelper commonFunctionHelper;

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlHelper urlHelper;

    @InjectMocks
    private UrlImplementation urlImplementation;

    @Test
    void redirectCountsTheClick() {
        givenActiveUrl();

        assertThat(urlImplementation.getOriginalUrl("abcd1234")).isEqualTo("https://example.com");
        verify(urlRepository).incrementClickCount(eq(1L), any());
    }

    @Test
    void redirectStillWorksWhenCountingFails() {
        givenActiveUrl();
        when(urlRepository.incrementClickCount(eq(1L), any())).thenThrow(new RuntimeException("db down"));

        assertThat(urlImplementation.getOriginalUrl("abcd1234")).isEqualTo("https://example.com");
    }

    @Test
    void unknownCodeIsNotCounted() {
        when(urlRepository.findByCodeAndStatusAndExpiresAtAfterAndDeleted(eq("missing"), eq(UrlStatusEnums.ACTIVE),
                any(), eq(false))).thenReturn(Optional.empty());

        assertThat(urlImplementation.getOriginalUrl("missing")).isNull();
        verify(urlRepository, never()).incrementClickCount(anyLong(), any());
    }

    private void givenActiveUrl() {
        UrlMasterEntity url = new UrlMasterEntity();
        url.setId(1L);
        url.setUrl("https%3A%2F%2Fexample.com");
        when(urlRepository.findByCodeAndStatusAndExpiresAtAfterAndDeleted(eq("abcd1234"), eq(UrlStatusEnums.ACTIVE),
                any(), eq(false))).thenReturn(Optional.of(url));
        when(urlHelper.decodeUrl("https%3A%2F%2Fexample.com")).thenReturn("https://example.com");
    }
}
