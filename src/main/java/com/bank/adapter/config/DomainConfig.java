package com.bank.adapter.config;

import com.bank.domain.config.BankAccountProperties;
import com.bank.domain.port.out.AccountPort;
import com.bank.domain.port.out.OperationPort;
import com.bank.domain.service.AccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public AccountService accountService(
            AccountPort accountPort,
            OperationPort operationPort,
            BankAccountProperties bankAccountProperties
    ) {

        return new AccountService(accountPort, operationPort, bankAccountProperties);
    }

}
