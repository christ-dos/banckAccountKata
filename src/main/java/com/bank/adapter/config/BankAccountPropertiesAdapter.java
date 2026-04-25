package com.bank.adapter.config;

import com.bank.domain.config.BankAccountProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Spring adapter for bank account properties.
 * Implements domain configuration by reading from application.yml
 */
@Configuration
@ConfigurationProperties(prefix = "bank.account")
public class BankAccountPropertiesAdapter extends BankAccountProperties {
}
