package com.example.urlshortener.service;

import com.example.urlshortener.dto.AnalyticsResponse;
import com.example.urlshortener.dto.CreateUrlResponse;
import com.example.urlshortener.entity.UrlMapping;
import com.example.urlshortener.event.ClickEvent;
import com.example.urlshortener.exception.UrlNotFoundException;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UrlService {
    private static final Logger log = LoggerFactory.getLogger(UrlService.class);
    private static final char[] ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private final UrlMappingRepository repo;
    private final StringRedisTemplate redis;
    private final KafkaTemplate<String, ClickEvent> kafka;
    private final String baseUrl;
    private final String topic;

    public UrlService(UrlMappingRepository repo,
                      StringRedisTemplate redis,
                      KafkaTemplate<String, ClickEvent> kafka,
                      @Value("${app.base-url}") String baseUrl,
                      @Value("${app.analytics-topic}") String topic) {
        this.repo = repo;
        this.redis = redis;
        this.kafka = kafka;
        this.baseUrl = baseUrl;
        this.topic = topic;
    }

    @Transactional
    public CreateUrlResponse create(String url) {
        validateUrl(url);
        for (int i = 0; i < 5; i++) {
            String code = randomCode(7);
            try {
                repo.saveAndFlush(new UrlMapping(url, code));
                try {
                    redis.opsForValue().set("url:" + code, url, Duration.ofMinutes(30));
                } catch (RuntimeException ex) {
                    log.warn("Failed writing cache for code {}: {}", code, ex.getMessage());
                }
                return new CreateUrlResponse(code, baseUrl + "/" + code, url);
            } catch (DataIntegrityViolationException ignored) {}
        }
        throw new IllegalStateException("Unable to allocate a unique short code");
    }

    public String resolve(String code, String userAgent, String referrer) {
        String cachedUrl = null;
        try {
            cachedUrl = redis.opsForValue().get("url:" + code);
        } catch (RuntimeException ex) {
            log.warn("Redis read failure for code {}: {}", code, ex.getMessage());
        }

        if (cachedUrl != null) {
            publishClickEventAsync(code, userAgent, referrer);
            return cachedUrl;
        }

        UrlMapping mapping = find(code);

        try {
            redis.opsForValue().set("url:" + code, mapping.getOriginalUrl(), Duration.ofMinutes(30));
        } catch (RuntimeException ex) {
            log.warn("Redis populate failure for code {}: {}", code, ex.getMessage());
        }

        publishClickEventAsync(code, userAgent, referrer);

        return mapping.getOriginalUrl();
    }

    public AnalyticsResponse analytics(String code) {
        UrlMapping m = find(code);
        return new AnalyticsResponse(m.getShortCode(), m.getOriginalUrl(), m.getClickCount());
    }

    private UrlMapping find(String code) {
        return repo.findByShortCode(code).orElseThrow(() -> new UrlNotFoundException(code));
    }

    private void publishClickEventAsync(String code, String userAgent, String referrer) {
        try {
            kafka.send(topic, code, new ClickEvent(code, Instant.now(), userAgent, referrer));
        } catch (RuntimeException ex) {
            log.warn("Kafka dispatch failure for code {}: {}", code, ex.getMessage());
        }
    }

    private static void validateUrl(String value) {
        try {
            URI uri = URI.create(value);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null) {
                throw new IllegalArgumentException("Only valid HTTP/HTTPS URLs are supported");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Only valid HTTP/HTTPS URLs are supported");
        }
    }

    private static String randomCode(int n) {
        StringBuilder b = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            b.append(ALPHABET[ThreadLocalRandom.current().nextInt(ALPHABET.length)]);
        }
        return b.toString();
    }
}