# Architecture Decision Records

This document records significant architecture decisions for Catch-Up Platform.
Each record captures the context, decision, and consequences so that future
changes can revisit decisions with their original trade-offs in view.

## Process

- Records are numbered sequentially and use the `ADR-NNNN` identifier.
- New records are appended; accepted records are not silently rewritten.
- When a decision changes, add a new ADR that supersedes the old one and update
  its status to `Superseded` with a link to the replacement.
- Use `Proposed`, `Accepted`, `Rejected`, `Deprecated`, or `Superseded` as status.
- Keep records focused on durable decisions, not implementation notes or
  temporary tasks.

## Index

| ADR                                                              | Title                                      | Status   |
|------------------------------------------------------------------|--------------------------------------------|----------|
| [ADR-0001](#adr-0001-layered-domain-driven-design-structure)     | Layered Domain-Driven Design structure     | Accepted |
| [ADR-0002](#adr-0002-mysql-with-spring-data-jpa-for-persistence) | MySQL with Spring Data JPA for persistence | Accepted |
| [ADR-0003](#adr-0003-rest-api-contract-and-error-responses)      | REST API contract and error responses      | Accepted |
| [ADR-0004](#adr-0004-redact-news-api-keys-in-application-logs)   | Redact News API keys in application logs   | Accepted |

## ADR-0001: Layered Domain-Driven Design structure

- **Status:** Accepted
- **Date:** 2026-10-15

### Context

The service manages favorite news-source mappings and has domain concepts such
as `FavoriteSource`, `NewsApiKey`, and `SourceId`. The code also needs to
separate HTTP concerns, use-case orchestration, domain behavior, and persistence
integration.

### Decision

Organize the code into `interfaces`, `application`, `domain`, and
`infrastructure` layers within the `news` bounded context, with cross-cutting
components in `shared`. Keep command and query service contracts in the
application layer and their implementations under `application.internal`.
Represent commands and queries as domain-model request records; application
services orchestrate use cases and return domain results.

### Consequences

- REST controllers depend on application contracts rather than persistence
  implementations.
- Application services coordinate repositories and translate persistence
  outcomes into application results.
- Domain types remain separate from HTTP resources and Spring MVC response
  types.
- The current code has separate command and query services, but shares the
  same persistence model; this does not imply separate read/write databases.

## ADR-0002: MySQL with Spring Data JPA for persistence

- **Status:** Accepted
- **Date:** 2026-10-15

### Context

Favorite-source mappings must persist across service restarts and enforce
uniqueness for each `(newsApiKey, sourceId)` pair. The application uses
Spring Boot and models the mapping as a JPA aggregate.

### Decision

Use MySQL for the relational database and Spring Data JPA/Hibernate for
persistence. Persist `FavoriteSource` through `FavoriteSourceRepository`,
convert `NewsApiKey` and `SourceId` value objects to strings with JPA attribute
converters, and enforce pair uniqueness with a named database constraint.
Use the shared Hibernate physical naming strategy to pluralize table names and
convert identifiers to snake case.

### Consequences

- Database-level uniqueness remains the final guard against concurrent
  duplicate creates; the application also performs an existence pre-check for
  a quicker conflict response.
- Persistence tests and application-context tests require a configured,
  available database unless explicitly isolated from MySQL.
- The domain value objects remain independent of their string storage
  representation.

## ADR-0003: REST API contract and error responses

- **Status:** Accepted
- **Date:** 2026-10-15

### Context

Clients need stable HTTP semantics for creating and retrieving favorite
sources, including actionable validation and lookup errors. HTTP response
types should remain outside the domain and application models.

### Decision

Expose the service under `/api/v1/favorite-sources` as a REST API. Return
`201 Created` for successful creation, `409 Conflict` for duplicate creation,
`200 OK` for successful retrievals (including an empty list), `404 Not Found`
for missing single-resource lookups, and `400 Bad Request` for invalid input.
Represent errors as Spring `ProblemDetail` responses and resolve user-facing
messages through `MessageSource`. Keep request/response resources and HTTP
response assemblers in the REST interface layer.

### Consequences

- HTTP status and payload translation is localized to the interface layer.
- Error details can be localized using the request locale.
- Changes to these status codes or response shapes are API contract changes
  and should be reflected in API documentation and compatibility review.

## ADR-0004: Redact News API keys in application logs

- **Status:** Accepted
- **Date:** 2026-10-15

### Context

The `newsApiKey` identifies a NewsAPI.org key and is used in request and
application-service logs for operational correlation. Logging its full value
would unnecessarily expose it in log storage.

### Decision

Use `NewsApiKey.maskedForLogging()` whenever logging a key. The current
representation reveals only the last four characters, or a fixed mask for
values of four characters or fewer. Do not log the raw key.

### Consequences

- Logs retain limited correlation information without including the complete
  key.
- The key remains stored as provided and is included in API resources where
  required by the current contract; log masking does not replace database
  protection, access control, or transport security.
- If a stronger secret-handling policy is adopted, revisit storage and API
  response exposure separately from this logging decision.
