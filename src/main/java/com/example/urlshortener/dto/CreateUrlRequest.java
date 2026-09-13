package com.example.urlshortener.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUrlRequest(
        @Schema(description = "Target URL to be shortened", example = "https://www.example.com/long-path")
        @NotBlank @Size(max=2048) String url
) {}

