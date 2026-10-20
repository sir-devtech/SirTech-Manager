package br.com.sirtech.manager;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base dos testes de integração: sobe UM PostgreSQL 16 real em Docker e aponta o Spring para ele
 * via {@link ServiceConnection}. As migrations do Flyway rodam de verdade, sem o seed (perfil dev
 * não é ativado).
 *
 * <p>Padrão "singleton container": o container é iniciado uma vez no bloco static e vive até a JVM
 * de testes terminar. Não usamos {@code @Testcontainers/@Container} aqui porque eles param o
 * container ao fim de cada classe, enquanto o Spring reaproveita o contexto (em cache) que ainda
 * aponta para a porta do container antigo, causando "Connection refused" na classe seguinte.
 */
@SpringBootTest
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }
}
