package S502VirtualPetApp.integration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class BaseMongoIntegrationTest {

    @Container
    protected static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.0").withReuse(false);

    static {
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        System.out.println("Mongo URI: " + mongoDBContainer.getReplicaSetUrl());
    }
}
