package com.example.urlshortener.event;

import java.time.Instant;

public record ClickEvent(String shortCode, Instant timestamp, String userAgent, String referrer) {}