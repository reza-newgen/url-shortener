# System Requirements Specifications

## Functional Requirements
* **URL Shortening:** System must generate a unique, 7-character Base62 short code for any valid HTTP/HTTPS URL.
* **Redirection:** Browsing to `/{shortCode}` must issue an HTTP 302 redirect to the target URL.
* **Analytics:** System must expose `GET /api/v1/urls/{code}/analytics` to return total redirect click counts.
* **API Documentation:** OpenAPI/Swagger spec must be exposed at `/swagger-ui.html`.

## Non-Functional Requirements
* **Low Latency:** Redirect path (`GET /{code}`) must resolve under 20ms using Redis caching.
* **Fault Tolerance:** Third-party failures (e.g., Kafka or Redis downtime) must not block or crash primary HTTP redirections.
* **Scalability:** Click count updates must execute asynchronously via Kafka to prevent database row-locking under heavy load.