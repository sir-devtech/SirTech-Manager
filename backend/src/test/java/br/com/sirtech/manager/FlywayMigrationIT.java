package br.com.sirtech.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class FlywayMigrationIT extends AbstractIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Test
    void migrationsSobemDoZeroSemPendencias() {
        // Se alguma migration falhasse, o contexto Spring nem subiria.
        assertThat(flyway.info().applied()).isNotEmpty();
        assertThat(flyway.info().pending()).isEmpty();
    }
}
