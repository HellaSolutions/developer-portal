package com.media.portal.developerportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DeveloperPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeveloperPortalApplication.class, args);
    }

}
