# Repository Architecture Guardrails for GitHub Copilot

## Tech Stack
- Java 21, Spring Boot 3.x, PostgreSQL, Spring Data Redis, Apache Kafka, Springdoc OpenAPI

## Core Architecture & Execution Rules
1. **Cache Read Path:** `UrlService.resolve()` MUST query Redis key `url:{code}` prior to issuing a database query.
2. **Resilience & Fault Isolation:** Redirection must remain non-blocking. Non-critical tasks (Kafka dispatches, Redis updates) must be wrapped in `try-catch` blocks to prevent third-party infrastructure exceptions from breaking URL redirects.
3. **Decoupled Analytics:** Do not execute synchronous database writes inside `resolve()`. Offload click counters to `AnalyticsConsumer` via Kafka events.
4. **Validation:** All incoming requests must be validated using Spring standard annotations and handled globally via `@RestControllerAdvice`.