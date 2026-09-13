package com.example.urlshortener.config;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;


@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic clickTopic(@Value("${app.analytics-topic}") String topic) {
        return new NewTopic(topic, 3, (short) 1);
    }
}