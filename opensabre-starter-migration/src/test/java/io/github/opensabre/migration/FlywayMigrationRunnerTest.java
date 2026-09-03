package io.github.opensabre.migration;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies strict parsing of deployment-supplied migration configuration.
 */
class FlywayMigrationRunnerTest {

    @Test
    void rejectsMissingRequiredConfiguration() throws Exception {
        Method required = FlywayMigrationRunner.class.getDeclaredMethod("required", Map.class, String.class);
        required.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> required.invoke(null, Map.of(), "FLYWAY_URL"));

        assertEquals("Missing required environment variable: FLYWAY_URL", exception.getCause().getMessage());
    }

    @Test
    void extractsTheSingleDatabaseSelectedByMysqlUrl() throws Exception {
        Method databaseFromUrl = FlywayMigrationRunner.class.getDeclaredMethod("databaseFromUrl", String.class);
        databaseFromUrl.setAccessible(true);

        assertEquals("flyway_test_auth",
                databaseFromUrl.invoke(null, "jdbc:mysql://mysql:3306/flyway_test_auth?useSSL=false"));
    }

    @Test
    void rejectsMysqlUrlWithoutDatabase() throws Exception {
        Method databaseFromUrl = FlywayMigrationRunner.class.getDeclaredMethod("databaseFromUrl", String.class);
        databaseFromUrl.setAccessible(true);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> databaseFromUrl.invoke(null, "jdbc:mysql://mysql:3306/?useSSL=false"));

        assertEquals("JDBC URL must select exactly one database: jdbc:mysql://mysql:3306/?useSSL=false",
                exception.getCause().getMessage());
    }
}
