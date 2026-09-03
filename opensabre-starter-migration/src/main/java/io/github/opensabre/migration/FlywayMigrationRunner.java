package io.github.opensabre.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.MigrateResult;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/**
 * Runs one application's Flyway migrations as a dedicated process.
 *
 * <p>The runner intentionally does not start Spring Boot. Deployments invoke it before the
 * corresponding application, using the migration scripts and dependencies already contained in
 * that application's immutable image.</p>
 */
public final class FlywayMigrationRunner {

    private static final String DEFAULT_LOCATIONS = "classpath:db/migration/mysql";

    private FlywayMigrationRunner() {
    }

    /**
     * Validates the configured migration history and migrates the database to the requested target.
     *
     * @param args unused; configuration is supplied through {@code FLYWAY_*} environment variables
     */
    public static void main(String[] args) {
        Map<String, String> environment = System.getenv();
        String url = required(environment, "FLYWAY_URL");
        String user = required(environment, "FLYWAY_USER");
        String password = required(environment, "FLYWAY_PASSWORD");
        String database = required(environment, "FLYWAY_DATABASE");
        String target = required(environment, "FLYWAY_TARGET");
        String locations = environment.getOrDefault("FLYWAY_LOCATIONS", DEFAULT_LOCATIONS);
        validateTarget(url, user, password, database);

        var configuration = Flyway.configure()
                .dataSource(url, user, password)
                .locations(split(locations))
                .schemas(database)
                .defaultSchema(database)
                .createSchemas(false)
                .validateMigrationNaming(true)
                .validateOnMigrate(true)
                .baselineOnMigrate(false)
                .cleanDisabled(true)
                .target(target);

        Flyway flyway = configuration.load();
        flyway.validate();
        MigrateResult result = flyway.migrate();
        MigrationInfo current = flyway.info().current();
        String currentVersion = current == null || current.getVersion() == null
                ? "none"
                : current.getVersion().getVersion();
        System.out.printf("Flyway migration completed: database=%s, migrations=%d, current=%s%n",
                result.database, result.migrationsExecuted, currentVersion);
    }

    private static String required(Map<String, String> environment, String name) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required environment variable: " + name);
        }
        return value;
    }

    /**
     * Ensures the JDBC URL and the connected MySQL session both point at the explicitly approved
     * database. SQL migrations are separately linted to forbid database-switching statements.
     */
    private static void validateTarget(String url, String user, String password, String expectedDatabase) {
        String urlDatabase = databaseFromUrl(url);
        if (!expectedDatabase.equals(urlDatabase)) {
            throw new IllegalArgumentException("FLYWAY_DATABASE does not match JDBC URL database: expected="
                    + expectedDatabase + ", actual=" + urlDatabase);
        }

        try (var connection = DriverManager.getConnection(url, user, password)) {
            String connectedDatabase = connection.getCatalog();
            if (!expectedDatabase.equals(connectedDatabase)) {
                throw new IllegalStateException("Connected database does not match FLYWAY_DATABASE: expected="
                        + expectedDatabase + ", actual=" + connectedDatabase);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to verify Flyway database target", exception);
        }
    }

    private static String databaseFromUrl(String url) {
        if (!url.startsWith("jdbc:mysql:")) {
            throw new IllegalArgumentException("Only jdbc:mysql URLs are supported: " + url);
        }
        try {
            String path = new URI(url.substring("jdbc:".length())).getPath();
            if (path == null || path.length() <= 1 || path.indexOf('/', 1) >= 0) {
                throw new IllegalArgumentException("JDBC URL must select exactly one database: " + url);
            }
            return path.substring(1);
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Invalid JDBC URL: " + url, exception);
        }
    }

    private static String[] split(String locations) {
        return locations.lines()
                .flatMap(line -> java.util.Arrays.stream(line.split(",")))
                .map(String::trim)
                .filter(location -> !location.isEmpty())
                .toArray(String[]::new);
    }
}
