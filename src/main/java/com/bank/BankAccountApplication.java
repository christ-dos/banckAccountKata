package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/**
 * Main class for the Bank Account application.
 * Spring Boot application entry point.
 */
@SpringBootApplication
public class BankAccountApplication {

    /**
     * Application entry point.
     * Sets UTC as default timezone to ensure consistent date/time handling across environments.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(BankAccountApplication.class, args);
    }
}
