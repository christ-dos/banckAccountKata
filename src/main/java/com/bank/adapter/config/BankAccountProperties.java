package com.bank.adapter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Configuration properties for bank accounts business rules.
 */
@Configuration
@ConfigurationProperties(prefix = "bank.account")
@Getter
@Setter
public class BankAccountProperties {

    private SavingsAccountProperties savings = new SavingsAccountProperties();

    @Getter
    @Setter
    public static class SavingsAccountProperties {
        /**
         * Default deposit limit for savings accounts (e.g., Livret A).
         * Default value: 22,950 EUR
         */
        private BigDecimal defaultDepositLimit = BigDecimal.valueOf(22950);
    }
}
