package no.property;

import org.springframework.boot.SpringApplication;

public class TestPropertyApplication {

    public static void main(String[] args) {
        SpringApplication.from(PropertyApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
