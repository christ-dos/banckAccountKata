package com.bank.adapter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Component to display startup information after application is ready.
 * Logs useful URLs and configuration details for developers.
 */
@Component
public class StartupLogger {

    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    private final Environment environment;

    public StartupLogger(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupInfo() {
        String port = environment.getProperty("server.port", "8080");
        String appName = environment.getProperty("spring.application.name", "bank-account-api");
        String h2Console = environment.getProperty("spring.h2.console.path", "/h2-console");
        String datasourceUrl = environment.getProperty("spring.datasource.url", "N/A");

        log.info("");
        log.info("==========================================================");
        log.info("🚀 APPLICATION STARTED SUCCESSFULLY: {}", appName);
        log.info("==========================================================");
        log.info("🌐 API URL:        http://localhost:{}", port);
        log.info("💾 H2 Console:     http://localhost:{}{}", port, h2Console);
        log.info("🗄️  Database:       {}", datasourceUrl);
        log.info("📡 API Endpoints:  http://localhost:{}/v1/accounts", port);
        log.info("==========================================================");
        log.info("");
    }
}
