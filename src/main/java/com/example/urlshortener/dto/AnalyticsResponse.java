package com.example.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record AnalyticsResponse(
        @Schema(description = "Short code", example = "aB3x9z1") String shortCode,
        @Schema(description = "Original URL", example = "https://www.example.com/long-path") String originalUrl,
        @Schema(description = "Total click count", example = "42") long clicks
) {}