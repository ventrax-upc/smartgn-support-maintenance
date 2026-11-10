package com.ventrax.support;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".+")
class DatabaseTest {
    @Test
    void migrationsAndConstraints() throws Exception {
        var f =
                org.flywaydb.core.Flyway.configure()
                        .dataSource(
                                System.getenv("TEST_DB_URL"),
                                "postgres",
                                System.getenv("TEST_DB_PASSWORD"))
                        .load();
        f.migrate();
        f.validate();
        try (var c =
                        java.sql.DriverManager.getConnection(
                                System.getenv("TEST_DB_URL"),
                                "postgres",
                                System.getenv("TEST_DB_PASSWORD"));
                var s = c.createStatement()) {
            var r = s.executeQuery("select count(*) from flyway_schema_history where success");
            r.next();
            assertEquals(4, r.getInt(1));
            assertThrows(
                    java.sql.SQLException.class,
                    () ->
                            s.execute(
                                    "insert into service_ratings"
                                        + " values(gen_random_uuid(),gen_random_uuid(),6,null,now())"));
        }
    }

    @Test
    void legacyAdoptionPreservesRows() throws Exception {
        String schema = "legacy_" + java.util.UUID.randomUUID().toString().replace("-", "");
        String url = System.getenv("TEST_DB_URL"), password = System.getenv("TEST_DB_PASSWORD");
        try (var c = java.sql.DriverManager.getConnection(url, "postgres", password);
                var statement = c.createStatement()) {
            statement.execute("create schema " + schema);
            statement.execute("set search_path to " + schema);
            org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(
                    c,
                    new org.springframework.core.io.ClassPathResource(
                            "db/migration/V1__initial_support.sql"));
            statement.execute(
                    "insert into knowledge_articles"
                        + " values(gen_random_uuid(),'Preserved','Safety','local-manual',now(),true)");
        }
        var f =
                org.flywaydb.core.Flyway.configure()
                        .dataSource(url, "postgres", password)
                        .schemas(schema)
                        .defaultSchema(schema)
                        .baselineVersion("1")
                        .load();
        f.baseline();
        f.migrate();
        f.validate();
        try (var c = java.sql.DriverManager.getConnection(url, "postgres", password);
                var s = c.createStatement();
                var r = s.executeQuery("select title from " + schema + ".knowledge_articles")) {
            assertTrue(r.next());
            assertEquals("Preserved", r.getString(1));
        }
    }
}
