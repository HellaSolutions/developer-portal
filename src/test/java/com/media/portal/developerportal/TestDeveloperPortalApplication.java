package com.media.portal.developerportal;

import org.springframework.boot.SpringApplication;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

public class TestDeveloperPortalApplication {

    public static void main(String[] args) {

        SpringApplication.from(DeveloperPortalApplication::main).with(TestcontainersConfiguration.class).run(args);

    }

}
