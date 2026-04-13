# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Campus Second-hand Trading Platform** (校园二手交易平台 / xywpgx) — a Spring Cloud microservices application for university students to buy, sell, borrow, and lend items. Includes a campus community forum and AI chatbot module.

- **Language**: Java 17
- **Framework**: Spring Boot 2.7.2 (microservices) / Spring Boot 3.4.1 (AI module)
- **Spring Cloud**: 2021.0.3 + Spring Cloud Alibaba 2021.0.1.0
- **Build Tool**: Maven (multi-module)

## Architecture

```
                    [Client/Frontend]
                          |
                    [Gateway :10010]
                    (Spring Cloud Gateway)
                          |
          auth    user    item    order   notif   storage  review   campus
         :8081   :8082   :8083   :8084   :8085   :8086    :8087    :8089
                          |
              [Nacos]  [MySQL 8.0 + Redis + RabbitMQ]
```

### Modules (11 total)

| Module | Port | Description |
|---|---|---|
| `common` | — | Shared utilities, auto-configurations, DTOs, exceptions, validation |
| `auth` | 8081 | Authentication (JWT, gateway SDK, resource SDK) |
| `gateway` | 10010 | Spring Cloud Gateway — routes by URL prefix (`/as/`, `/us/`, `/is/`, etc.) |
| `user` | 8082 | User management (registration, profile, stats, real-name verification) |
| `item` | 8083 | Item/category management (CRUD, search, stats) |
| `order` | 8084 | Second-hand transaction orders (create, pay, confirm, cancel, refund) |
| `notification` | 8085 | System broadcasts and user read-status |
| `storage` | 8086 | File upload to Cloudflare R2 (S3-compatible) |
| `review` | 8087 | Review/rating system for completed transactions |
| `campus` | 8089 | Campus community (topics, categories, comments, announcements) |
| `ai` | — | AI chat service (LangChain4j, SSE streaming, Spring Boot 3.4.1 standalone) |

### Package Convention

All packages follow `com.aynu.<module>.<layer>`:
- `controller` — REST controllers
- `service` / `service.impl` — Service interfaces and implementations
- `mapper` — MyBatis mappers
- `domain.po` — Persistent objects (database entities)
- `domain.dto` — Data transfer objects
- `domain.vo` — View objects

Cross-service Feign clients are in the `api` module: `com.aynu.api.client.*`

### Middleware Stack

- **Registry/Config**: Nacos
- **ORM**: MyBatis-Plus 3.5.5
- **Cache**: Redis + Redisson 3.13.6 (distributed locking)
- **RPC**: OpenFeign with Sentinel circuit breaker
- **Message Queue**: RabbitMQ
- **Distributed Transactions**: Seata 1.5.1
- **Search**: Elasticsearch 7.12.1
- **Task Scheduling**: XXL-Job 2.3.1
- **API Docs**: Knife4j
- **Object Storage**: Cloudflare R2 (AWS SDK v2 S3)
- **Payment**: Alipay SDK

## Key Commands

### Build

```bash
# Build all modules (skip tests)
mvn clean package -DskipTests

# Build specific service modules with dependencies
mvn clean package -DskipTests -pl auth/auth-service,gateway,user,item,order,notification,storage,review -am

# Build a single module
mvn clean package -DskipTests -pl order
```

### Run Individual Services

Each service is a Spring Boot application. Run via Maven or your IDE:

```bash
# Example: run the order service
mvn spring-boot:run -pl order
```

Ensure Nacos, MySQL, Redis, and RabbitMQ are running first.

### Docker Deployment

```bash
# Deploy all services (see deploy-all.sh)
./deploy-all.sh

# Deploy a single service
./deploy.sh <service-name>

# Deploy AI module (separate script)
./deploy-ai.sh
```

### API Documentation

Each service exposes Knife4j Swagger UI at `http://<host>:<port>/doc.html`. Full API reference is in `README.md`.

## Development Notes

- **Gateway routes** are configured in `gateway/src/main/resources/bootstrap.yml`
- **Service configs** are loaded from Nacos config center (profiles activated via `spring.profiles.active`)
- **Inter-service calls** go through Feign clients defined in the `api` module (with Sentinel fallback)
- **Authentication** flows through the gateway (JWT validation via auth-gateway-sdk), with per-service validation via auth-resource-sdk
- The **AI module** uses Spring Boot 3.4.1 with WebFlux (reactive), unlike the rest of the system which uses Spring Boot 2.7.2 with servlet stack
- **Database migrations**: Check each module's `src/main/resources/mapper/` directory for MyBatis XML mapper files
