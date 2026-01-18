package com.bank.adapter.config;

import com.bank.domain.port.out.AccountPort;
import com.bank.domain.service.AccountService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public AccountService accountService(AccountPort accountPort) {
        return new AccountService(accountPort);
    }

}
