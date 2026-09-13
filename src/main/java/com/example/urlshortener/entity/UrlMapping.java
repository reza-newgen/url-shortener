package com.example.urlshortener.entity;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="url_mapping", indexes=@Index(name="idx_short_code", columnList="shortCode", unique=true))
public class UrlMapping {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=2048)
    private String originalUrl;

    @Column(nullable=false, unique=true, length=16)
    private String shortCode;

    @Column(nullable=false)
    private Instant createdAt;

    @Column(nullable=false)
    private long clickCount;

    protected UrlMapping() {}
    public UrlMapping(String originalUrl, String shortCode) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.createdAt = Instant.now();
        this.clickCount = 0;
    }

    public Long getId() { return id; }
    public String getOriginalUrl() { return originalUrl; }
    public String getShortCode() { return shortCode; }
    public Instant getCreatedAt() { return createdAt; }
    public long getClickCount() { return clickCount; }
    public void incrementClicks() { this.clickCount++; }
}