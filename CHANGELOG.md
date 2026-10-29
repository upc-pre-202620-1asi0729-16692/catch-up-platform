# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

[1.0.0] 2026-10-29

### Added

- Layered Domain-Driven Design (DDD) architecture with `news` and `shared` bounded contexts.
- `FavoriteSource` aggregate root entity with `NewsApiKey` and `SourceId` domain value objects.
- JPA Attribute Converters (`NewsApiKeyAttributeConverter`, `SourceIdAttributeConverter`) for transparent database persistence.
- JPA Auditing support with auto-generated entity identifiers and timestamps.
- Application-layer CQRS service contracts (`FavoriteSourceCommandService`, `FavoriteSourceQueryService`) and implementations.
- Shared `Result` abstraction pattern for functional error handling and command outcome encapsulation.
- REST API controller `FavoriteSourcesController` with endpoints:
  - `POST /api/v1/favorite-sources`: Create a new favorite source.
  - `GET /api/v1/favorite-sources/{id}`: Retrieve a favorite source by ID.
  - `GET /api/v1/favorite-sources?newsApiKey={key}`: List favorite sources by News API key.
  - `GET /api/v1/favorite-sources?newsApiKey={key}&sourceId={sourceId}`: Retrieve favorite source by composite key.
- Assembler components to map between REST request/response resources, domain commands/queries, and Spring `ResponseEntity` / `ProblemDetail`.
- Custom `SnakeCasePhysicalNamingStrategy` for converting table names to pluralized snake_case.
- `GlobalExceptionHandler` and localized validation/error messages in English (`messages.properties`) and Spanish (`messages_es.properties`).
- Environment-specific Spring profiles (`dev`, `prod`) and Docker container configuration (`Dockerfile`).
- SpringDoc OpenAPI / Swagger UI integration for interactive API documentation.
- Project documentation including Architecture Decision Records (ADRs), PlantUML class diagram, user stories, and contributing guidelines.

[1.0.0]: https://github.com/upc-pre-202620-1asi0729-16692/catch-up-platform/releases/tag/v1.0.0
