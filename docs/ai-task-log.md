# AI Copilot Task Log & Engineering Audits

| Prompts / Tasks | Copilot Initial Output | Engineer Modifications & Fixes |
| :--- | :--- | :--- |
| **Kafka Config** | Used hardcoded default property strings in `Environment.getProperty()`. | Refactored to inject Spring Boot `KafkaProperties` bean directly. |
| **Unit Test Specs** | Included untyped `any()` in `ValueOperations.set()`, causing ambiguous compiler errors. | Updated parameter matching to `any(Duration.class)`. |
| **Cache Integration** | Executed synchronous DB write calls inside `resolve()`. | Refactored click counter increment to async Kafka event consumer pattern. |