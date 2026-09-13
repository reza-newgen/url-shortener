# Architectural Trade-Offs & Risk Analysis

## 1. Eventual Consistency for Analytics
* **Trade-off:** Click counters are updated asynchronously via Kafka consumers rather than transactional DB writes on redirect.
* **Benefit:** Eliminates database write bottlenecks and row contention on hot URLs.
* **Risk:** Slight delay in reflecting real-time click counts on the analytics endpoint.

## 2. Base62 Random Allocation vs. Hashing
* **Trade-off:** Random string generation with collision retries vs. deterministic hashing algorithm (e.g., MD5/SHA-256).
* **Benefit:** Prevents predictable URL enumeration attacks.
* **Risk:** Minor chance of collision under high key space utilization, mitigated by unique DB indexes and retry attempts.