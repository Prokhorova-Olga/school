package ru.hogwarts.school.controller;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootTest
public class SomeTest {
    private static final Logger log = LoggerFactory.getLogger(SomeTest.class);

    @Autowired
    private DataSource dataSource;

    @Test
    void checkDatabase() throws SQLException {
        log.info("Database URL: {}", dataSource.getConnection().getMetaData().getURL());
    }
}