package com.library;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LibraryApplication {

    private static final Logger log = LoggerFactory.getLogger(LibraryApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
        log.info("Library Management backend started successfully");
    }
}
