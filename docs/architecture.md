# System Architecture & Design

## System Overview
```text
[ Client ] ──> [ Spring Boot REST API / UrlController ]
                        │
       ┌────────────────┼────────────────┐
       ▼                ▼                ▼
[ Redis Cache ]  [ PostgreSQL ]  [ Kafka Producer ]
 (Cache-Aside)   (Persistence)   (Click Analytics)
                                         │
                                         ▼
                               [ AnalyticsConsumer ] ──> [ PostgreSQL ]