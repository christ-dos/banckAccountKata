package com.bank.adapter.config;

import com.bank.domain.config.BankAccountProperties;
import com.bank.domain.port.out.AccountPort;
import com.bank.domain.port.out.OperationPort;
import com.bank.domain.service.AccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for domain layer beans.
 */
@Configuration
public class DomainConfig {

    /**
     * Creates the AccountService bean with its dependencies.
     *
     * @param accountPort the account port implementation
     * @param operationPort the operation port implementation
     * @param bankAccountProperties the bank account properties
     * @return the configured AccountService
     */
    @Bean
    public AccountService accountService(
            AccountPort accountPort,
            OperationPort operationPort,
            BankAccountProperties bankAccountProperties
    ) {

        return new AccountService(accountPort, operationPort, bankAccountProperties);
    }

}
