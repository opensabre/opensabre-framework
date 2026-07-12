# Repository Guidelines

## Project Structure & Module Organization

This is a Java 17 multi-module Maven project for Opensabre framework starters. The root `pom.xml` aggregates modules and controls the shared `${revision}` version. Dependency versions live in `opensabre-base-dependencies`.

- `opensabre-web`: shared web models, validators, converters, and exceptions.
- `opensabre-starter-*`: Spring Boot auto-configuration starters for boot, cache, config, EDA, persistence, register, RPC, and Web MVC features.
- `opensabre-test`: reusable test helpers.
- `src/main/java`, `src/test/java`, `src/main/resources`: source, tests, and starter resources such as `opensabre-*.yml`, `*.properties`, Spring auto-configuration imports, and metadata JSON files.

## Build, Test, and Development Commands

- `mvn clean install`: build and install all modules locally.
- `mvn test`: run the full test suite.
- `mvn -pl opensabre-web test`: test one module; add `-am` when dependencies must also be built.
- `mvn test -Dtest=UserContextHolderTest`: run a single JUnit test class.
- `mvn -B package javadoc:javadoc --file pom.xml`: mirror the GitHub Actions package and Javadoc build.
- `mvn clean deploy -Pdeploy`: publish artifacts; requires Sonatype/GPG credentials.
- `mvn flatten:flatten`: generate the CI-friendly flattened POM file named `pom-xml-flattened`.

## Coding Style & Naming Conventions

Use UTF-8 and Java 17. Follow the existing Java style: 4-space indentation, one public top-level class per file, packages under `io.github.opensabre`, and concise JavaDoc for public APIs. Lombok is used where it removes boilerplate. Keep starter configuration classes named `Opensabre*Config` and resource files named by module, for example `opensabre-rpc.yml`.

Entity suffixes are meaningful: `Po` for persistence objects, `Vo` for responses, `Form` for request forms, and `Param` for query or service parameters.

## Testing Guidelines

Tests use JUnit 5 with Maven Surefire. Place tests under each module's `src/test/java`, matching the production package. Name test classes `*Test` and prefer focused unit tests for utilities, exception handlers, desensitizers, converters, and auto-configuration behavior. Update tests when changing shared base classes, Spring configuration, or error handling.

## Commit & Pull Request Guidelines

Recent commits use short Chinese summaries such as `新增审计功能模块` and `优化jib打包，httpclient5升级至5.4.1`. Keep commits imperative and scoped to one change. Mention version bumps when changing `${revision}` or dependency versions.

Pull requests should include a concise description, affected modules, test commands run, and linked issues when applicable. Include screenshots only for documentation or API UI changes. Note publishing, configuration, or migration impact.

## Agent-Specific Instructions

Do not rewrite generated outputs or unrelated user changes. When adding dependencies, update `opensabre-base-dependencies` first and avoid hard-coded versions in child modules unless the existing module pattern requires it.
