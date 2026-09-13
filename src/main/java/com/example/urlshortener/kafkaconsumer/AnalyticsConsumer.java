package com.example.urlshortener.kafkaconsumer;

import com.example.urlshortener.event.ClickEvent;
import com.example.urlshortener.repository.UrlMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AnalyticsConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsConsumer.class);
    private final UrlMappingRepository repository;

    public AnalyticsConsumer(UrlMappingRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "${app.analytics-topic}", groupId = "url-analytics-group")
    @Transactional
    public void handleClickEvent(ClickEvent event) {
        repository.findByShortCode(event.shortCode()).ifPresentOrElse(
                mapping -> {
                    mapping.incrementClicks();
                    repository.save(mapping);
                    log.debug("Incremented click count for shortCode: {}", event.shortCode());
                },
                () -> log.warn("Analytics event received for unknown shortCode: {}", event.shortCode())
        );
    }
}