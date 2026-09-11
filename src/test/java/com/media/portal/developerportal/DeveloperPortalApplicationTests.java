package com.media.portal.developerportal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DeveloperPortalApplicationTests {

    @Autowired
    private DataSource dataSource;


    @Test
    void contextLoads() {
    }


    @Test
    void shouldConnectToPostgres() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println(connection.getMetaData().getURL());
            assertEquals("PostgreSQL", connection.getMetaData().getDatabaseProductName());
        }
    }
}

