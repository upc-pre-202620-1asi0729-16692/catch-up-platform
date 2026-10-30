# Catch-Up Platform — Study Guide

> A learning resource for Software Engineering Students.  
> This guide analyzes the **Catch-Up Platform** codebase — a Spring Boot REST API — and explains the approaches, patterns, frameworks, and language features applied, so you can replicate them in your own projects.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Architecture & Design Patterns](#2-architecture--design-patterns)
3. [Domain-Driven Design (DDD)](#3-domain-driven-design-ddd)
4. [CQRS — Command Query Responsibility Segregation](#4-cqrs---command-query-responsibility-segregation)
5. [Spring Boot Framework](#5-spring-boot-framework)
6. [Java Language Features](#6-java-language-features)
7. [Persistence Layer — JPA & Hibernate](#7-persistence-layer---jpa--hibernate)
8. [REST API Design](#8-rest-api-design)
9. [Validation & Error Handling](#9-validation--error-handling)
10. [Internationalization (i18n)](#10-internationalization-i18n)
11. [Configuration & Profiles](#11-configuration--profiles)
12. [Containerization — Docker](#12-containerization---docker)
13. [Build System — Maven](#13-build-system---maven)
14. [Testing](#14-testing)
15. [Recommended Learning Resources](#15-recommended-learning-resources)

---

## 1. Project Overview

**Catch-Up Platform** is a Spring Boot web service that provides a REST API for managing users' favorite news sources. It integrates with [NewsAPI.org](https://newsapi.org/) by storing mappings between API keys and source identifiers.

| Aspect         | Detail                           |
|----------------|----------------------------------|
| **Language**   | Java 27                          |
| **Framework**  | Spring Boot 4.1.1                |
| **Build Tool** | Apache Maven 3.10.0              |
| **Database**   | MySQL 8+                         |
| **ORM**        | Spring Data JPA (Hibernate)      |
| **API Docs**   | SpringDoc OpenAPI 3 / Swagger UI |
| **License**    | MIT                              |

### Key Features

- Create and retrieve favorite news sources
- Scoped queries by News API key
- Duplicate detection with database constraints
- Localized error messages (English and Spanish)
- Swagger UI for interactive API documentation

---

## 2. Architecture & Design Patterns

### 2.1 Layered Architecture with Bounded Contexts

The project follows a **layered architecture** organized into **bounded contexts** (a DDD concept). The package structure reflects a clear separation:

```
com.acme.catchup.platform
├── news                          ← Bounded context: "News"
│   ├── domain                    ← Domain layer
│   │   └── model
│   │       ├── aggregates        ← Aggregate roots
│   │       ├── commands          ← Command objects
│   │       ├── queries           ← Query objects
│   │       └── valueobjects      ← Value objects
│   ├── application               ← Application layer
│   │   ├── commandservices       ← Command service interfaces
│   │   ├── queryservices         ← Query service interfaces
│   │   └── internal              ← Service implementations
│   ├── interfaces                ← Interface (presentation) layer
│   │   └── rest
│   │       ├── resources         ← DTOs / Resource records
│   │       └── transform         ← Assemblers / Transformers
│   └── infrastructure            ← Infrastructure layer
│       └── persistence
│           └── jpa
│               ├── converters    ← JPA attribute converters
│               └── repositories  ← Spring Data repositories
└── shared                        ← Shared bounded context
    ├── application
    │   └── patterns              ← Shared patterns (Result)
    ├── interfaces
    │   └── rest                  ← Shared REST utilities
    └── infrastructure
        └── persistence
            └── jpa
                └── strategy      ← Shared JPA strategies
```

**Why this matters:** This structure enforces **separation of concerns**. Each layer depends only on the layer below it, and the domain layer has **zero dependencies** on frameworks. This makes the codebase testable, maintainable, and portable.

### 2.2 Dependency Injection

The project uses **constructor-based dependency injection** throughout. Spring automatically wires beans by injecting dependencies through constructors:

```java
public FavoriteSourceCommandServiceImpl(FavoriteSourceRepository favoriteSourceRepository) {
    this.favoriteSourceRepository = favoriteSourceRepository;
}
```

**Key benefits:**
- Immutable dependencies (`final` fields)
- Clear, explicit dependencies
- Easy unit testing (no framework needed to instantiate)
- No `@Autowired` on fields — considered a best practice

**Reference:** [Spring — Dependency Injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies.html)

### 2.3 The Result Pattern (Either/Outcome Pattern)

File: `shared/application/patterns/Result.java`

The `Result<T, E>` type is a **functional result container** that represents either a success with a value of type `T` or a failure with an error of type `E`. It eliminates the need for exceptions in business logic flow:

```java
public sealed interface Result<T, E> permits Result.Success, Result.Failure {
    record Success<T, E>(T value) implements Result<T, E> {}
    record Failure<T, E>(E error) implements Result<T, E> {}

    static <T, E> Result<T, E> success(T value) { ... }
    static <T, E> Result<T, E> failure(E error) { ... }

    default boolean isSuccess() { ... }
    default boolean isFailure() { ... }
    default <R> R fold(Function<? super T, ? extends R> onSuccess,
                       Function<? super E, ? extends R> onFailure) { ... }
}
```

**How it's used:** The command service returns `Result<FavoriteSource, FavoriteSourceCommandFailure>`, and the interface layer uses `fold()` to map the result to an HTTP response:

```java
return result.fold(
    source -> new ResponseEntity<>(resource, HttpStatus.CREATED),
    failure -> ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, message))
);
```

**Why this matters:** This pattern — also known as **Either**, **Outcome**, or **Railway-oriented programming** — makes error handling **explicit** and **type-safe**. Callers must handle both success and failure paths, which prevents unhandled exceptions.

**Reference:** [Railway-Oriented Programming](https://fsharpforfunandprofit.com/rop/)

### 2.4 Assembler / Transformer Pattern

The `transform` package contains dedicated classes that convert objects between layers:

| Assembler                                                | Purpose                        |
|----------------------------------------------------------|--------------------------------|
| `NewsValueObjectFromStringAssembler`                     | `String` → Domain Value Object |
| `CreateFavoriteSourceCommandFromResourceAssembler`       | Resource → Command             |
| `FavoriteSourceResourceFromEntityAssembler`              | Entity → Resource              |
| `ResponseEntityFromFavoriteSourceCommandResultAssembler` | Result → ResponseEntity        |
| `ResponseEntityFromFavoriteSourceQueryResultAssembler`   | Query Result → ResponseEntity  |

**Why this matters:** Assemblers keep transformation logic out of controllers and services, following the **Single Responsibility Principle**. They make the boundary-crossing code explicit and testable.

---

## 3. Domain-Driven Design (DDD)

### 3.1 Bounded Contexts

The codebase is divided into two bounded contexts:

- **`news`** — Contains all domain logic related to favorite news sources
- **`shared`** — Contains cross-cutting concerns (Result pattern, exception handling, JPA naming strategy)

Each bounded context has its own domain model, application services, and infrastructure.

### 3.2 Aggregate Root

File: `news/domain/model/aggregates/FavoriteSource.java`

`FavoriteSource` is an **aggregate root** — the main entity that represents a cluster of associated objects treated as a single unit for data changes:

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"newsApiKey", "sourceId"},
                      name = "uk_favorite_source_news_api_key_source_id")
})
public class FavoriteSource extends AbstractAggregateRoot<FavoriteSource> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Convert(converter = NewsApiKeyAttributeConverter.class)
    private NewsApiKey newsApiKey;

    @Column(nullable = false)
    @Convert(converter = SourceIdAttributeConverter.class)
    private SourceId sourceId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
```

**Key DDD concepts demonstrated:**
- **Aggregate Root** as the entry point for all modifications
- **Domain constructor** that takes a command object (not raw primitives)
- **Protected no-arg constructor** for JPA framework use only
- **Uniqueness constraint** enforced at the database level

### 3.3 Value Objects

Files: `NewsApiKey.java`, `SourceId.java`

Value objects are **immutable** objects defined by their attributes, not by an identity:

```java
public record NewsApiKey(String value) {
    public NewsApiKey {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("News API key cannot be null or blank");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("News API key cannot exceed " + MAX_LENGTH + " characters");
        }
    }

    public String maskedForLogging() {
        if (value.length() <= 4) return "****";
        return "****" + value.substring(value.length() - 4);
    }
}
```

**Key characteristics:**
- **Immutability** — Records are inherently immutable
- **Validation in constructor** — Invariants are enforced at creation time
- **Domain behavior** — `maskedForLogging()` is a domain-specific operation
- **No identity** — Two `NewsApiKey` instances with the same value are interchangeable

**Why this matters:** Value objects encapsulate business rules and prevent invalid states. Instead of passing raw strings everywhere, the code uses typed value objects that guarantee validity.

### 3.4 Domain Events (Aggregate Root Inheritance)

`FavoriteSource` extends `AbstractAggregateRoot<FavoriteSource>`, which is the Spring Data base class for aggregate roots that supports **domain events**. While not explicitly published in this codebase, the infrastructure is in place for domain event publishing.

**Reference:** [Martin Fowler — Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)

---

## 4. CQRS — Command Query Responsibility Segregation

The application separates **commands** (operations that change state) from **queries** (operations that read state):

### 4.1 Commands

| Component                          | Role                           |
|------------------------------------|--------------------------------|
| `CreateFavoriteSourceCommand`      | Command object (record)        |
| `FavoriteSourceCommandService`     | Command service interface      |
| `FavoriteSourceCommandServiceImpl` | Command service implementation |
| `FavoriteSourceCommandFailure`     | Sealed failure types           |

Commands return a `Result<T, E>` to explicitly model success or failure.

### 4.2 Queries

| Component                                       | Role                         |
|-------------------------------------------------|------------------------------|
| `GetAllFavoriteSourcesByNewsApiKeyQuery`        | Query object                 |
| `GetFavoriteSourceByIdQuery`                    | Query object                 |
| `GetFavoriteSourceByNewsApiKeyAndSourceIdQuery` | Query object                 |
| `FavoriteSourceQueryService`                    | Query service interface      |
| `FavoriteSourceQueryServiceImpl`                | Query service implementation |

Queries return `List<T>` or `Optional<T>`.

### 4.3 Service Interface vs. Implementation Separation

Notice the package structure:

```
news/application/commandservices/          ← Interfaces (contracts)
news/application/internal/commandservices/ ← Implementations
```

**Why this matters:** Defining interfaces at the application boundary and placing implementations in an `internal` subpackage follows the **Dependency Inversion Principle**. The interface defines *what* the service does; the implementation details *how*. This enables:
- Swapping implementations without changing callers
- Clear module boundaries
- Potential for CQRS scaling (separate read/write models)

**Reference:** [CQRS Journey](https://learn.microsoft.com/en-us/azure/architecture/guide/architecture-cqrs)

---

## 5. Spring Boot Framework

### 5.1 Auto-Configuration

File: `CatchUpPlatformApplication.java`

```java
@EnableJpaAuditing
@SpringBootApplication
public class CatchUpPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(CatchUpPlatformApplication.class, args);
    }
}
```

`@SpringBootApplication` combines three annotations:
- `@Configuration` — Marks the class as a bean definition source
- `@EnableAutoConfiguration` — Tells Spring Boot to autoconfigure beans based on the classpath
- `@ComponentScan` — Scans for components in the current package and sub-packages

`@EnableJpaAuditing` enables automatic population of `@CreatedDate` and `@LastModifiedDate` fields.

### 5.2 Spring Stereotype Annotations

| Annotation              | Used On                  | Purpose                         |
|-------------------------|--------------------------|---------------------------------|
| `@Service`              | Service implementations  | Marks business logic components |
| `@Repository`           | Repository interfaces    | Marks data access components    |
| `@RestController`       | Controllers              | Marks REST API endpoints        |
| `@Converter`            | JPA converters           | Marks attribute converter beans |
| `@RestControllerAdvice` | Global exception handler | Marks global exception handlers |

### 5.3 Spring Data JPA Repositories

File: `FavoriteSourceRepository.java`

```java
@Repository
public interface FavoriteSourceRepository extends JpaRepository<FavoriteSource, Long> {
    List<FavoriteSource> findAllByNewsApiKey(NewsApiKey newsApiKey);
    boolean existsByNewsApiKeyAndSourceId(NewsApiKey newsApiKey, SourceId sourceId);
    Optional<FavoriteSource> findByNewsApiKeyAndSourceId(NewsApiKey newsApiKey, SourceId sourceId);
}
```

**Key concept:** Spring Data JPA generates implementations at runtime from method names. No implementation class is needed — Spring derives the query from the method name using **query derivation**.

**Reference:** [Spring Data JPA — Query Methods](https://docs.spring.io/spring-data/jpa/reference/query.html)

### 5.4 Transaction Management

```java
@Override
@Transactional
public Result<FavoriteSource, FavoriteSourceCommandFailure> handle(CreateFavoriteSourceCommand command) {
    // ...
}
```

`@Transactional` ensures the entire method executes within a single database transaction. If an exception occurs, the transaction is rolled back automatically.

### 5.5 Lombok Annotations

| Annotation | Purpose                                 |
|------------|-----------------------------------------|
| `@Getter`  | Generates getter methods for all fields |
| `@Slf4j`   | Creates a `log` field (SLF4J Logger)    |

Lombok reduces boilerplate by generating common code at compile time via annotation processing.

**Reference:** [Project Lombok](https://projectlombok.org/)

---

## 6. Java Language Features

### 6.1 Records (Java 16+)

Records are the most prominent Java feature used in this codebase. They provide **concise immutable data carriers**:

```java
public record CreateFavoriteSourceCommand(NewsApiKey newsApiKey, SourceId sourceId) {
    public CreateFavoriteSourceCommand {
        if (newsApiKey == null) throw new IllegalArgumentException("News API key cannot be null");
        if (sourceId == null) throw new IllegalArgumentException("Source ID cannot be null");
    }
}
```

**What records provide automatically:**
- Constructor
- `equals()`, `hashCode()`, `toString()`
- Accessor methods (`newsApiKey()`, `sourceId()`)
- Immutability (all fields are `final`)

**Compact constructors** allow validation logic without repeating field declarations.

**Used for:** Commands, queries, resources, and failure types.

**Reference:** [JEP 395 — Records](https://openjdk.org/jeps/395)

### 6.2 Sealed Interfaces (Java 17+)

```java
public sealed interface Result<T, E> permits Result.Success, Result.Failure {
    record Success<T, E>(T value) implements Result<T, E> {}
    record Failure<T, E>(E error) implements Result<T, E> {}
}

public sealed interface FavoriteSourceCommandFailure permits FavoriteSourceCommandFailure.Duplicate {
    String messageKey();
    record Duplicate() implements FavoriteSourceCommandFailure { ... }
}
```

**Sealed interfaces** restrict which classes/records can implement them. This enables:
- **Exhaustive pattern matching** — the compiler can verify all cases are handled
- **Explicit domain modeling** — only known failure types can exist
- **Better documentation** — the permitted subclasses are declared at the interface level

**Reference:** [JEP 409 — Sealed Types](https://openjdk.org/jeps/409)

### 6.3 Pattern Matching with `instanceof` (Java 16+)

```java
default boolean isSuccess() {
    return this instanceof Success<?, ?>;
}

default Optional<T> success() {
    if (this instanceof Success<?, ?> success) {
        T value = ((Success<T, E>) success).value();
        return Optional.of(value);
    }
    return Optional.empty();
}
```

Pattern matching eliminates explicit casting after type checks.

**Reference:** [JEP 394 — Pattern Matching for instanceof](https://openjdk.org/jeps/394)

### 6.4 `Optional` (Java 8+)

Used throughout the query layer to represent potentially absent values:

```java
Optional<FavoriteSource> handle(GetFavoriteSourceByIdQuery query);
```

**Why this matters:** `Optional` makes the absence of a value explicit in the type system, reducing `NullPointerException` risks.

### 6.5 Streams & Method References (Java 8+)

```java
var favoriteSourceResources = favoriteSources.stream()
    .map(FavoriteSourceResourceFromEntityAssembler::toResourceFromEntity)
    .toList();
```

**Method references** (`ClassName::methodName`) provide concise functional expressions.

### 6.6 `var` Local Variable Type Inference (Java 10+)

```java
var favoriteSource = new FavoriteSource(command);
var createdFavoriteSource = favoriteSourceRepository.save(favoriteSource);
```

`var` reduces verbosity while maintaining type safety (inferred at compile time).

### 6.7 Text Blocks & String Templates (Java 21+)

While not heavily used in this codebase, Java 27 supports modern string features. The codebase uses traditional string concatenation for log messages.

---

## 7. Persistence Layer — JPA & Hibernate

### 7.1 Entity Mapping

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"newsApiKey", "sourceId"},
                      name = "uk_favorite_source_news_api_key_source_id")
})
public class FavoriteSource extends AbstractAggregateRoot<FavoriteSource> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // ...
}
```

| Annotation                           | Purpose                              |
|--------------------------------------|--------------------------------------|
| `@Entity`                            | Marks the class as a JPA entity      |
| `@Table`                             | Specifies table name and constraints |
| `@Id`                                | Marks the primary key field          |
| `@GeneratedValue`                    | Defines ID generation strategy       |
| `@Column`                            | Specifies column properties          |
| `@Convert`                           | Links to an attribute converter      |
| `@CreatedDate` / `@LastModifiedDate` | Auto-populated audit timestamps      |
| `@EntityListeners`                   | Registers entity lifecycle listeners |

### 7.2 Attribute Converters

Files: `NewsApiKeyAttributeConverter.java`, `SourceIdAttributeConverter.java`

Converters bridge the gap between domain value objects and database column types:

```java
@Converter
public class NewsApiKeyAttributeConverter implements AttributeConverter<NewsApiKey, String> {
    @Override
    public String convertToDatabaseColumn(NewsApiKey attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public NewsApiKey convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new NewsApiKey(dbData);
    }
}
```

**Why this matters:** Converters let you use rich domain types (value objects) in your entities while JPA handles the translation to/from database columns transparently.

### 7.3 Custom Physical Naming Strategy

File: `SnakeCasePhysicalNamingStrategy.java`

```java
public class SnakeCasePhysicalNamingStrategy implements PhysicalNamingStrategy {
    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(this.toPlural(logicalName));
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        return this.toSnakeCase(logicalName);
    }
}
```

This strategy converts:
- `FavoriteSource` → `favorite_sources` (pluralized snake_case table name)
- `newsApiKey` → `news_api_key` (snake_case column name)

**Why this matters:** Database conventions often differ from Java naming conventions. A custom naming strategy eliminates the need for manual `@Table(name = "...")` annotations on every entity.

### 7.4 JPA Auditing

Enabled by `@EnableJpaAuditing` on the main class and `@EntityListeners(AuditingEntityListener.class)` on entities:

```java
@CreatedDate
private Instant createdAt;

@LastModifiedDate
private Instant updatedAt;
```

Spring automatically populates these fields on entity creation and update.

---

## 8. REST API Design

### 8.1 REST Endpoints

| Method | Endpoint                                               | Description              |
|--------|--------------------------------------------------------|--------------------------|
| `POST` | `/api/v1/favorite-sources`                             | Create a favorite source |
| `GET`  | `/api/v1/favorite-sources/{id}`                        | Get by ID                |
| `GET`  | `/api/v1/favorite-sources?newsApiKey=...`              | Get all by API key       |
| `GET`  | `/api/v1/favorite-sources?newsApiKey=...&sourceId=...` | Get by key + source      |

### 8.2 Controller Design

```java
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/favorite-sources", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Favorite Sources", description = "Endpoints for favorite sources")
public class FavoriteSourcesController {
    // ...
}
```

**Key patterns:**
- **API versioning** via URL path (`/api/v1/`)
- **Content type declaration** (`produces = APPLICATION_JSON_VALUE`)
- **Delegation to application services** — controllers are thin
- **Assembler-based transformation** — no mapping logic in controllers
- **OpenAPI annotations** for documentation

### 8.3 HTTP Status Codes

| Status            | When                             |
|-------------------|----------------------------------|
| `201 Created`     | Successful creation              |
| `200 OK`          | Successful retrieval             |
| `400 Bad Request` | Validation errors, invalid input |
| `404 Not Found`   | Resource not found               |
| `409 Conflict`    | Duplicate favorite source        |

### 8.4 RFC 7807 ProblemDetail

Error responses use the standard `ProblemDetail` format:

```java
ResponseEntity.status(status)
    .body(ProblemDetail.forStatusAndDetail(status, localizedMessage));
```

**Why this matters:** RFC 7807 provides a standardized error response format that clients can parse consistently.

**Reference:** [RFC 7807 — Problem Details for HTTP APIs](https://datatracker.ietf.org/doc/html/rfc7807)

### 8.5 OpenAPI / Swagger Documentation

```java
@Operation(summary = "Create a favorite source", description = "...")
@ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Favorite source created", ...),
    @ApiResponse(responseCode = "409", description = "Conflict", ...)
})
```

SpringDoc OpenAPI automatically generates interactive API documentation at `/swagger-ui/index.html`.

**Reference:** [SpringDoc OpenAPI](https://springdoc.org/)

---

## 9. Validation & Error Handling

### 9.1 Jakarta Bean Validation

File: `CreateFavoriteSourceResource.java`

```java
public record CreateFavoriteSourceResource(
    @NotBlank(message = "{favorite.source.error.newsApiKey.notBlank}")
    @Size(max = 256, message = "{favorite.source.error.newsApiKey.size}")
    @Pattern(regexp = "^[a-zA-z0-9-_:-]+$", message = "{favorite.source.error.newsApiKey.pattern}")
    String newsApiKey,

    @NotBlank(message = "{favorite.source.error.sourceId.notBlank}")
    @Size(max = 256, message = "{favorite.source.error.sourceId.size}")
    @Pattern(regexp = "^[a-zA-z0-9-_:-]+$", message = "{favorite.source.error.sourceId.pattern}")
    String sourceId) {}
```

| Annotation                 | Validates                        |
|----------------------------|----------------------------------|
| `@NotBlank`                | String is not null and not blank |
| `@Size(max = 256)`         | String length ≤ 256              |
| `@Pattern(regexp = "...")` | String matches the regex pattern |

Validation is triggered by `@Valid` on the controller method parameter:

```java
@PostMapping
public ResponseEntity<?> createFavoriteSource(@Valid @RequestBody CreateFavoriteSourceResource resource) {
```

### 9.2 Multi-Level Validation Strategy

The codebase validates at **three levels**:

1. **Interface layer** — Bean Validation on resources (HTTP boundary)
2. **Domain layer** — Value object constructors enforce invariants
3. **Database layer** — Unique constraints prevent duplicates

This **defense-in-depth** approach ensures invalid data is caught as early as possible.

### 9.3 Global Exception Handler

File: `GlobalExceptionHandler.java`

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleException(MethodArgumentNotValidException exception, Locale locale) {
        // Collects and localizes all field errors
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse handleException(IllegalArgumentException exception, Locale locale) {
        // Resolves message key from exception message
    }
}
```

`@RestControllerAdvice` provides **global** exception handling across all controllers. Instead of repeating try-catch blocks, exceptions are centralized.

### 9.4 Duplicate Detection Strategy

The command service uses a **two-tier** approach:

```java
// Pre-check: Application-level check
if (favoriteSourceRepository.existsByNewsApiKeyAndSourceId(...)) {
    return duplicateResult();
}

// Post-check: Database constraint violation
try {
    var created = favoriteSourceRepository.save(favoriteSource);
    return Result.success(created);
} catch (DataIntegrityViolationException exception) {
    if (isDuplicateFavoriteSourceViolation(exception)) {
        return duplicateResult();
    }
    throw exception;
}
```

**Why both checks?** The pre-check is a fast path that avoids unnecessary database writes. The post-check handles **race conditions** where two concurrent requests pass the pre-check simultaneously.

---

## 10. Internationalization (i18n)

### 10.1 Message Bundles

| File                     | Language          |
|--------------------------|-------------------|
| `messages.properties`    | English (default) |
| `messages_es.properties` | Spanish           |

### 10.2 Message Resolution

```java
private static String localizedMessage(MessageSource messageSource, String messageKey) {
    return messageSource.getMessage(messageKey, null, messageKey, LocaleContextHolder.getLocale());
}
```

The `MessageSource` bean resolves messages based on the current request's locale (from the `Accept-Language` header). If a translation is missing, the key itself is returned as a fallback.

### 10.3 Parameterized Messages

```properties
favorite.source.error.notFoundById=Favorite source isn’t found for id {0}.
```

```java
messageSource.getMessage(messageKey, new Object[]{id}, locale)
```

**Reference:** [Spring — Externalizing Message](https://docs.spring.io/spring-framework/reference/core/internationalization.html)

---

## 11. Configuration & Profiles

### 11.1 Spring Profiles

| Profile | File                          | Use Case                                       |
|---------|-------------------------------|------------------------------------------------|
| Default | `application.properties`      | Base configuration                             |
| `dev`   | `application-dev.properties`  | Local development (SQL logging on, SSL off)    |
| `prod`  | `application-prod.properties` | Production (SQL logging off, SSL on, env vars) |

### 11.2 Environment Variables

```properties
spring.datasource.username=${DATABASE_USER}
spring.datasource.password=${DATABASE_PASSWORD}
```

Sensitive configuration is externalized via environment variables, following the **12-Factor App** methodology.

### 11.3 Profile Activation

```bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

Or programmatically via `application.properties`:
```properties
spring.profiles.active=dev
```

**Reference:** [Spring Boot — Profiles](https://docs.spring.io/spring-boot/reference/features/dev-properties.html#features.dev-properties.active-profiles)

---

## 12. Containerization — Docker

### 12.1 Multi-Stage Build

File: `Dockerfile`

```dockerfile
# Stage 1: Build
FROM maven:3.10.0-eclipse-temurin-27 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:27-jre AS runtime
ENV SPRING_PROFILES_ACTIVE=prod
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Why multi-stage?** The build stage contains Maven and the JDK (large image). The runtime stage uses only the JRE (small image), resulting in a smaller final container.

### 12.2 Key Docker Concepts Demonstrated

| Concept               | Example                                  |
|-----------------------|------------------------------------------|
| Multi-stage builds    | `AS build` / `AS runtime`                |
| Layer caching         | `COPY pom.xml` before `COPY src`         |
| Environment variables | `ENV SPRING_PROFILES_ACTIVE=prod`        |
| Port exposure         | `EXPOSE 8080`                            |
| Entry point           | `ENTRYPOINT ["java", "-jar", "app.jar"]` |

**Reference:** [Docker — Multi-stage builds](https://docs.docker.com/build/building/multi-stage/)

---

## 13. Build System — Maven

### 13.1 Parent POM Inheritance

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.1</version>
</parent>
```

Inheriting from `spring-boot-starter-parent` provides:
- Managed dependency versions (no version needed for Spring Boot starters)
- Default plugin configurations
- Sensible defaults for resource filtering and more

### 13.2 Dependency Management

| Dependency                            | Scope    | Purpose                      |
|---------------------------------------|----------|------------------------------|
| `spring-boot-starter-web`             | compile  | REST API, embedded Tomcat    |
| `spring-boot-starter-data-jpa`        | compile  | JPA/Hibernate integration    |
| `spring-boot-starter-validation`      | compile  | Bean Validation              |
| `mysql-connector-j`                   | runtime  | MySQL JDBC driver            |
| `lombok`                              | provided | Compile-time code generation |
| `spring-boot-starter-test`            | test     | JUnit, Mockito, etc.         |
| `springdoc-openapi-starter-webmvc-ui` | compile  | Swagger UI                   |

### 13.3 Annotation Processing

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <release>${java.version}</release>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Lombok is configured as an annotation processor, running at compile time to generate boilerplate code.

**Reference:** [Maven — Dependency Scopes](https://maven.apache.org/guides/introduction/introduction-to-dependency-management.html)

---

## 14. Testing

### 14.1 Application Context Test

File: `CatchUpPlatformApplicationTests.java`

```java
@SpringBootTest
class CatchUpPlatformApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

This is a **smoke test** that verifies the Spring application context loads successfully. It validates:
- All beans can be created and wired
- Database connection is available
- Configuration is valid

### 14.2 Testing Strategies to Apply

While the codebase includes only a basic context test, the architecture supports several testing approaches:

| Layer           | Strategy                                                                     |
|-----------------|------------------------------------------------------------------------------|
| **Domain**      | Unit tests for value objects, aggregates, and commands (no framework needed) |
| **Application** | Unit tests for service implementations with mocked repositories              |
| **Interface**   | Integration tests with `@SpringBootTest` and `@AutoConfigureMockMvc`         |
| **Repository**  | Integration tests with `@DataJpaTest` and an embedded database               |

**Reference:** [Spring Boot — Testing](https://docs.spring.io/spring-boot/reference/testing/testing.html)

---

## 15. Recommended Learning Resources

### Java Language

| Topic                  | Resource                                                |
|------------------------|---------------------------------------------------------|
| Records (JEP 395)      | [OpenJDK JEP 395](https://openjdk.org/jeps/395)         |
| Sealed Types (JEP 409) | [OpenJDK JEP 409](https://openjdk.org/jeps/409)         |
| Pattern Matching       | [OpenJDK JEP 394](https://openjdk.org/jeps/394)         |
| Modern Java Features   | [Baeldung — Java Guides](https://www.baeldung.com/java) |

### Spring Boot

| Topic                      | Resource                                                                                            |
|----------------------------|-----------------------------------------------------------------------------------------------------|
| Getting Started            | [Spring Boot Guides](https://spring.io/guides)                                                      |
| Dependency Injection       | [Spring Framework — Beans](https://docs.spring.io/spring-framework/reference/core/beans.html)       |
| Spring Data JPA            | [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/reference/)                      |
| Testing                    | [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/testing.html)            |
| Externalized Configuration | [Spring Boot Properties](https://docs.spring.io/spring-boot/reference/features/dev-properties.html) |

### Domain-Driven Design

| Topic             | Resource                                                                                                                  |
|-------------------|---------------------------------------------------------------------------------------------------------------------------|
| DDD Introduction  | [Martin Fowler — DDD](https://martinfowler.com/bliki/DomainDrivenDesign.html)                                             |
| Aggregate Pattern | [Vaughn Vernon — Implementing DDD](https://www.oreilly.com/library/view/implementing-domain-driven-design/9780321834577/) |
| Value Objects     | [DDD Patterns — Value Object](https://dddcommunity.org/DDD/Definitions/ValueObject.html)                                  |
| Bounded Contexts  | [DDD Patterns — Bounded Context](https://dddcommunity.org/DDD/Definitions/BoundedContext.html)                            |

### Architecture & Patterns

| Topic                    | Resource                                                                                         |
|--------------------------|--------------------------------------------------------------------------------------------------|
| CQRS                     | [Microsoft — CQRS](https://learn.microsoft.com/en-us/azure/architecture/guide/architecture-cqrs) |
| Result/Either Pattern    | [Railway-Oriented Programming](https://fsharpforfunandprofit.com/rop/)                           |
| Layered Architecture     | [Martin Fowler — Layered Architecture](https://martinfowler.com/architecture/layer.html)         |
| REST API Design          | [RESTful API Design Tutorial](https://restfulapi.net/)                                           |
| RFC 7807 Problem Details | [RFC 7807](https://datatracker.ietf.org/doc/html/rfc7807)                                        |

### Docker & DevOps

| Topic              | Resource                                                                  |
|--------------------|---------------------------------------------------------------------------|
| Docker Basics      | [Docker Documentation](https://docs.docker.com/get-started/)              |
| Multi-stage Builds | [Docker Multi-stage](https://docs.docker.com/build/building/multi-stage/) |
| 12-Factor App      | [The 12-Factor App](https://12factor.net/)                                |

### Maven

| Topic                 | Resource                                                                                                              |
|-----------------------|-----------------------------------------------------------------------------------------------------------------------|
| Maven Basics          | [Maven Getting Started](https://maven.apache.org/guides/getting-started/)                                             |
| Dependency Management | [Maven Dependency Mediation](https://maven.apache.org/guides/introduction/introduction-to-dependency-management.html) |

---

## Summary Checklist

Use this checklist when starting your own projects to apply the patterns from this codebase:

- [ ] **Organize it by bounded contexts** — Group related functionality into clear modules
- [ ] **Use layered architecture** — Separate domain, application, interface, and infrastructure layers
- [ ] **Model with value objects** — Replace primitive types with typed, validated value objects
- [ ] **Apply CQRS** — Separate command (write) and query (read) operations
- [ ] **Use the Result pattern** — Return explicit success/failure instead of throwing exceptions for expected outcomes
- [ ] **Validate at multiple levels** — Interface, domain, and database layers
- [ ] **Externalize configuration** — Use environment variables for secrets
- [ ] **Use Spring profiles** — Separate dev, test, and prod configurations
- [ ] **Document with OpenAPI** — Annotate controllers for automatic API documentation
- [ ] **Handle errors globally** — Use `@RestControllerAdvice` for centralized exception handling
- [ ] **Containerize with Docker** — Use multi-stage builds for production images
- [ ] **Write tests** — At minimum, verify the application context loads

---

*Generated for the Open-Source Application Development course.*
