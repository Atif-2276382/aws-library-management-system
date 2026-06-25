package com.library;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
public class LibraryApplication {

    private static final Logger log = LoggerFactory.getLogger(LibraryApplication.class);

    @Value("${spring.datasource.url}")
private String dbUrl;

@PostConstruct
public void printDbUrl() {
    log.info("DATABASE = " + dbUrl);
}

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
        log.info("Library Management backend started successfully");
    }

       @EventListener(ApplicationReadyEvent.class)
    public void startup() {
        log.info("=== CUSTOM INFO LOG WORKING Event Service EMAIL  ===");
    }
}
