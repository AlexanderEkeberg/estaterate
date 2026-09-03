package no.estaterate;

import org.springframework.boot.SpringApplication;

public class TestEstaterateApplication {

    public static void main(String[] args) {
        SpringApplication.from(EstaterateApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
