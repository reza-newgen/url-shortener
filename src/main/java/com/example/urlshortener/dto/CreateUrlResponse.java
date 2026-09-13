package com.example.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateUrlResponse(
        @Schema(description = "Unique 7-character short code", example = "aB3x9z1") String shortCode,
        @Schema(description = "Full short URL location", example = "http://localhost:8080/aB3x9z1") String shortUrl,
        @Schema(description = "Original destination URL", example = "https://www.example.com/long-path") String originalUrl
) {}