package S502VirtualPetApp.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestMongoConfig {

    @Bean
    public MongoDBContainer mongoDBContainer() {
        MongoDBContainer container = new MongoDBContainer(
                DockerImageName.parse("mongo:7.0.0") // Versión compatible
        );
        container.start();
        return container;
    }
}