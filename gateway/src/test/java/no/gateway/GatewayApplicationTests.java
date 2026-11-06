package no.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Consul slås av her, så testen kan kjøres uten Docker
@SpringBootTest(properties = "spring.cloud.consul.enabled=false")
class GatewayApplicationTests {

    @Test
    void contextLoads() {
    }

}
