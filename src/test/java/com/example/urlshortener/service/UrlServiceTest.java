package com.example.urlshortener.service;


import com.example.urlshortener.dto.CreateUrlResponse;
import com.example.urlshortener.entity.UrlMapping;
import com.example.urlshortener.event.ClickEvent;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock private UrlMappingRepository repo;
    @Mock private StringRedisTemplate redis;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private KafkaTemplate<String, ClickEvent> kafka;

    private UrlService urlService;

    @BeforeEach
    void setUp() {
        lenient().when(redis.opsForValue()).thenReturn(valueOperations);
        urlService = new UrlService(repo, redis, kafka, "http://localhost:8080/api/v1/urls", "url-analytics-topic");
    }

    @Test
    void create_ValidUrl_ReturnsCreateUrlResponse() {
        when(repo.saveAndFlush(any(UrlMapping.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateUrlResponse response = urlService.create("https://example.com");

        assertNotNull(response);
        assertEquals("https://example.com", response.originalUrl());
        assertNotNull(response.shortCode());
        verify(repo, times(1)).saveAndFlush(any(UrlMapping.class));
    }

    @Test
    void resolve_CacheHit_ReturnsUrlWithoutDbLookup() {
        when(valueOperations.get("url:abc1234")).thenReturn("https://example.com");

        String result = urlService.resolve("abc1234", "Mozilla", "direct");

        assertEquals("https://example.com", result);
        verify(repo, never()).findByShortCode(anyString());
        verify(kafka, times(1)).send(eq("url-analytics-topic"), eq("abc1234"), any(ClickEvent.class));
    }

    @Test
    void resolve_CacheMiss_QueriesDbAndPopulatesCache() {
        when(valueOperations.get("url:abc1234")).thenReturn(null);
        UrlMapping mapping = new UrlMapping("https://example.com", "abc1234");
        when(repo.findByShortCode("abc1234")).thenReturn(Optional.of(mapping));

        String result = urlService.resolve("abc1234", "Mozilla", "direct");

        assertEquals("https://example.com", result);
        verify(repo, times(1)).findByShortCode("abc1234");
        verify(valueOperations, times(1)).set(eq("url:abc1234"), eq("https://example.com"), any(Duration.class));
    }
}