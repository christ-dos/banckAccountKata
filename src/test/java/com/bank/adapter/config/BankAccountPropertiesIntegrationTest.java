package com.bank.adapter.config;

import com.bank.domain.config.BankAccountProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test to verify that Spring loads BankAccountProperties from application.yml
 */
@SpringBootTest
class BankAccountPropertiesIntegrationTest {

    @Autowired
    private BankAccountProperties bankAccountProperties;

    @Test
    void shouldLoadPropertiesFromYaml() {
        // Given
        assertNotNull(bankAccountProperties, "BankAccountProperties should be injected by Spring");
        assertNotNull(bankAccountProperties.getSavings(), "Savings properties should be initialized");

        // When
        BigDecimal depositLimit = bankAccountProperties.getSavings().getDefaultDepositLimit();

        // Then
        assertEquals(new BigDecimal("22950"), depositLimit,
                "Deposit limit should be loaded from application.yml (22950)");
    }
}
