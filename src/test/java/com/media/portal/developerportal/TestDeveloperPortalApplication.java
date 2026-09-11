package com.media.portal.developerportal;

import org.springframework.boot.SpringApplication;

public class TestDeveloperPortalApplication {

    public static void main(String[] args) {

        SpringApplication.from(DeveloperPortalApplication::main).with(TestcontainersConfiguration.class).run(args);

    }

}
