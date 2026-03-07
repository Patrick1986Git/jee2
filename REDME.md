# enterprise-shop

Backend Spring Boot for an enterprise shop application.

## Tech stack
- Java
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven

## Project structure
Main source code is located in:

`src/main/java/com/company/shop`

Business modules are organized under:

- `module.cart`
- `module.category`
- `module.order`
- `module.product`
- `module.system`
- `module.user`

Shared infrastructure:
- `common`
- `config`
- `security`
- `validation`

## Profiles
Application profiles:
- `dev`
- `prod`

Configuration files:
- `application.yml`
- `application-dev.yml`
- `application-prod.yml`

## Database
Database migrations are managed with Flyway.

Migration scripts are located in:

`src/main/resources/db/migration`

## Running locally
### Requirements
- Java 17+ (or your project version)
- Maven
- Docker and Docker Compose
- PostgreSQL

### Start database
```bash
docker-compose up -d