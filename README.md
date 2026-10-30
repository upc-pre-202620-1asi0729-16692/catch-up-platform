# Catch-Up Platform (catch-up-platform)

## Overview

Catch-Up Platform is a Spring Boot service that provides a REST API to manage users' favorite news sources. The project is structured around Domain-Driven Design (DDD), organized into bounded contexts (`news` and `shared`), using application-layer command/query contracts, a shared `Result` abstraction for command outcomes, and JPA persistence.

This service is designed to work with [NewsAPI.org](https://newsapi.org/):
- `newsApiKey` refers to an API key issued by NewsAPI.org
- `sourceId` refers to a NewsAPI.org source identifier (for example, `bbc-news`)
- This service stores favorite source mappings; it does not proxy NewsAPI.org content endpoints

## Features

- List favorite sources scoped to a News API key
- Retrieve a favorite source by its identifier
- Retrieve a favorite source by News API key and source id
- Create (persist) a new favorite source
- Map command and query outcomes to HTTP responses through interface-layer response translators
- Custom Hibernate naming strategy to convert identifiers to snake_case and plural table names

## Technologies

- OpenJDK 27 and Spring Boot 4.1.1
- Apache Maven 3.10.0
- Spring Web (REST controllers)
- Spring Data JPA (Hibernate) with MySQL
- Spring Validation (Jakarta Bean Validation)
- SpringDoc OpenAPI 3 / Swagger UI
- Lombok (compile-time helpers)
- PlantUML (architecture diagrams in `docs/`)

## Prerequisites

- OpenJDK 27
- Apache Maven 3.10.0
- MySQL 8+ (database server)
- Docker (optional for local testing, recommended for deployment packaging)

## Environment variables

The application resolves credentials and connection details from environment variables. Set the following before running locally or in a container:

| Variable            | Description                                             |
|---------------------|---------------------------------------------------------|
| `DATABASE_USER`     | MySQL username                                          |
| `DATABASE_PASSWORD` | MySQL password                                          |
| `DATABASE_URL`      | Database host / hostname used by the production profile |
| `DATABASE_NAME`     | Database name (default: `catch-up-os`)                  |
| `DATABASE_PORT`     | Database port (production profile)                      |

## Spring profiles

| Profile     | Usage                                                   |
|-------------|---------------------------------------------------------|
| *(default)* | Uses `localhost:3306` with SSL enabled; SQL logging off |
| `dev`       | Uses `localhost:3306` with SSL disabled; SQL logging on |
| `prod`      | Builds the JDBC URL from env vars; used in Docker/CI    |

Set the active profile with:

```bash
export SPRING_PROFILES_ACTIVE=dev
```

## Technical stories

The API-focused technical stories for frontend integration are in [`docs/user-stories.md`](docs/user-stories.md).

## Class diagram

A PlantUML class diagram that reflects the code structure and bounded contexts is available at [`docs/class-diagram.puml`](docs/class-diagram.puml).

## Architecture Decision Records

Significant architecture decisions and their trade-offs are recorded in [`docs/adrs.md`](docs/adrs.md).

## Architecture notes

- `news.application.commandservices` and `news.application.queryservices` define the application-layer service contracts
- `news.application.internal.commandservices` and `news.application.internal.queryservices` contain the implementations
- `shared.application.result.Result` models command outcomes without introducing HTTP or domain concepts
- `news.interfaces.rest.transform.ResponseEntityFromFavoriteSourceCommandResultAssembler` maps command results to `ResponseEntity` values for the REST boundary
- `news.interfaces.rest.transform.ResponseEntityFromFavoriteSourceQueryResultAssembler` maps query results and localized query errors to `ResponseEntity` values for the REST boundary

## API documentation (Swagger UI)

When the application is running, interactive API documentation is available at:

```
http://localhost:8080/swagger-ui/index.html
```

## Error responses

- Validation and domain input errors return `400 Bad Request` with a `ProblemDetail` payload.
- Duplicate favorite creation returns `409 Conflict` with a localized `ProblemDetail` payload.
- Not-found lookups (`GET /api/v1/favorite-sources/{id}` and `GET /api/v1/favorite-sources?newsApiKey=...&sourceId=...`) return `404 Not Found` with a localized `ProblemDetail` payload.

## Getting started (quick)

Install the prerequisites, ensure MySQL is running locally, and export the database credentials. Then run with the `dev` profile:

```bash
export DATABASE_USER=your_db_user
export DATABASE_PASSWORD=your_db_password
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

For the `prod` profile, also provide:

- `DATABASE_URL` (database host / hostname)
- `DATABASE_PORT` (database port)
- `DATABASE_NAME` (database name)

Or build and run the jar:

```bash
mvn clean package
java -jar target/platform-1.0.0.jar
```

The test suite includes an application-context test that connects to the configured MySQL database. Set the database credentials and ensure MySQL is available before running `mvn test`.

## Run with Docker

The `Dockerfile` is intended for deployment-style packaging:
- it builds the application in a multi-stage image
- it runs the application with `SPRING_PROFILES_ACTIVE=prod` by default
- it expects database connection settings to be provided via environment variables at runtime

Build the container image:

```bash
docker build -t catch-up-platform:local .
```

Run the container (deployment-style execution; supply required runtime env vars):

```bash
docker run --rm -p 8080:8080 \
  -e DATABASE_USER=your_db_user \
  -e DATABASE_PASSWORD=your_db_password \
  -e DATABASE_URL=your_db_host \
  -e DATABASE_PORT=3306 \
  -e DATABASE_NAME=catch-up-os \
  -e SPRING_PROFILES_ACTIVE=prod \
  --name catch-up-platform \
  catch-up-platform:local
```

For cloud/container platforms, inject the same variables using the platform's secret/config mechanism instead of hardcoding values.

## Notes

- This repository intentionally reflects a focused subset of functionality (favorite management); delete operations are not currently implemented.
