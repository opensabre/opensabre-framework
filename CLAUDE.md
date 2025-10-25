# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Opensabre is a microservices development platform based on Spring Cloud 2023, integrating Spring Security, Spring Cloud Alibaba, and other components. It provides foundational RBAC permission management, authorization authentication, gateway management, service governance, audit logging, and other system management applications.

## Build and Development Commands

### Building the Project
```bash
# Build all modules
mvn clean install

# Build with tests
mvn clean install -DskipTests=false

# Skip tests for faster builds
mvn clean install -DskipTests=true

# Deploy to Maven Central (requires deploy profile)
mvn clean deploy -Pdeploy
```

### Testing
```bash
# Run all tests
mvn test

# Run tests for specific module
cd opensabre-web && mvn test

# Run single test class
mvn test -Dtest=UserContextHolderTest
```

### Code Quality
```bash
# Generate JavaDoc
mvn javadoc:javadoc

# Generate flattened POM (for deployment)
mvn flatten:flatten
```

## Architecture and Module Structure

### Core Modules

- **opensabre-base-dependencies**: Central dependency management with Spring Boot 3.4.1, Spring Cloud 2024.0.0, and Spring Cloud Alibaba 2023.0.3.2
- **opensabre-web**: Core web utilities including exception handling, validation, and entity converters
- **opensabre-starter-boot**: Auto-configuration for global exception handling, response wrapping, and sensitive data processing
- **opensabre-starter-rpc**: RPC components with OpenFeign configuration, load balancing, and Sentinel integration
- **opensabre-starter-persistence**: MyBatis Plus integration with base entity classes and meta object handlers
- **opensabre-starter-cache**: Redis caching with JetCache implementation
- **opensabre-starter-config**: Configuration management
- **opensabre-starter-register**: Service discovery and registration (Nacos)
- **opensabre-starter-eda**: Event-driven architecture components
- **opensabre-starter-webmvc**: Web MVC utilities and exception handling
- **opensabre-test**: Testing utilities

### Key Architectural Patterns

#### 1. Unified Response Format
All REST APIs return `Result<T>` objects with standardized structure:
- `code`: "000000" for success, other codes for errors
- `mesg`: Human-readable message
- `time`: Timestamp in UTC format
- `data`: Response payload (nullable)

#### 2. Entity Layering
- **BasePo**: Base persistence object with audit fields (createdBy, createdTime, updatedBy, updatedTime)
- **BaseVo**: Base view object for API responses
- **BaseForm**: Base form object for API requests
- **BaseQueryForm**: Base query form for search operations

#### 3. Exception Handling
- Global exception handling via `DefaultGlobalExceptionHandlerAdvice`
- Custom exception hierarchy with `BaseException` and `ServiceException`
- Error type system with `ErrorType` and `SystemErrorType`

#### 4. Sensitive Data Processing
- Log desensitization for sensitive fields (passwords, names)
- REST API response desensitization
- Configurable desensitization strategies

#### 5. Service Communication
- OpenFeign with custom load balancing (`OpensabreLoadBalancer`)
- Header propagation via `FeignHeaderInterceptor`
- Sentinel integration for circuit breaking

## Development Guidelines

### Adding New Modules
1. Create module directory under root
2. Add module to parent POM's `<modules>` section
3. Define dependencies in `opensabre-base-dependencies/pom.xml`
4. Use `@AutoConfiguration` for Spring Boot auto-configuration classes

### Creating REST Endpoints
- Extend from `BasePo` for persistence entities
- Use `Result<T>` for all API responses
- Implement proper exception handling
- Follow the entity conversion pattern: `Po -> Vo`

### Database Operations
- Extend `BasePo` for entity classes
- Use MyBatis Plus for ORM
- Implement `PoMetaObjectHandler` for audit field population

### Configuration
- Use YAML configuration files with `YamlPropertyLoaderFactory`
- Place configuration in `src/main/resources/`
- Follow naming convention: `opensabre-{module}.yml`

## Technology Stack

- **Java 17**: Primary development language
- **Spring Boot 3.4.1**: Application framework
- **Spring Cloud 2024.0.0**: Microservices framework
- **Spring Cloud Alibaba 2023.0.3.2**: Alibaba cloud components
- **MyBatis Plus 3.5.5**: ORM framework
- **JetCache 2.7.7**: Multi-level caching
- **Knife4j 4.5.0**: API documentation
- **Hutool 5.8.35**: Utility library
- **JUnit 5**: Testing framework

## Deployment

### Docker Support
- Dockerfile Maven plugin for image building
- Jib Maven plugin for containerization
- Base image: `eclipse-temurin:21-jre-alpine`

### Maven Central Publishing
- Uses `central-publishing-maven-plugin`
- Requires `deploy` profile activation
- Automatic source and JavaDoc packaging

## Testing Strategy

- Unit tests in `src/test/java` directories
- Integration tests for configuration validation
- Test utilities in `opensabre-test` module
- Use `@AutoConfiguration` for testing configuration classes