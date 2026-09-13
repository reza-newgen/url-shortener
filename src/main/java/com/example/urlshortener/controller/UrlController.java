package com.example.urlshortener.controller;

import com.example.urlshortener.dto.AnalyticsResponse;
import com.example.urlshortener.dto.CreateUrlRequest;
import com.example.urlshortener.dto.CreateUrlResponse;
import com.example.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/urls")
@Tag(name = "URL Shortener API", description = "Endpoints for creating, resolving, and monitoring short URLs")
public class UrlController {

    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    @Operation(summary = "Create short URL", description = "Generates a unique 7-character short code for a valid URL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "URL shortened successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid URL format provided")
    })
    @PostMapping("/")
    public ResponseEntity<CreateUrlResponse> create(@Valid @RequestBody CreateUrlRequest request) {
        return ResponseEntity.ok(service.create(request.url()));
    }

    @Operation(summary = "Redirect short URL", description = "Redirects GET request to original target URL.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirecting to original target"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    @GetMapping("/{code}")
    public RedirectView redirect(@PathVariable String code, HttpServletRequest request) {
        String target = service.resolve(code, request.getHeader("User-Agent"), request.getHeader("Referer"));
        RedirectView view = new RedirectView(target);
        view.setExposeModelAttributes(false);
        return view;
    }

    @Operation(summary = "Get URL Analytics", description = "Returns total click metrics and details for a short code.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Short code not found")
    })
    @GetMapping("/{code}/analytics")
    public AnalyticsResponse analytics(@PathVariable String code) {
        return service.analytics(code);
    }
}