package com.bank.adapter.in.rest.controller;

import com.bank.adapter.in.rest.request.CreateAccountRequest;
import com.bank.adapter.in.rest.request.SetOverdraftLimitRequest;
import com.bank.domain.model.AccountType;
import com.bank.domain.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for setOverdraftLimit endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SetOverdraftLimitIntegrationTest {

    private static final String EUR = "EUR";
    private static final String USD = "USD";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountService accountService;

    // ========================================
    // HELPER METHODS
    // ========================================

    /**
     * Helper method to create an account and return its ID.
     */
    private String createAccount(AccountType accountType, String currency) throws Exception {
        CreateAccountRequest createRequest = new CreateAccountRequest(accountType, currency);
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(createResponse).get("accountId").asText();
    }

    // ========================================
    // OVERDRAFT LIMIT CONFIGURATION TESTS
    // ========================================

    @Test
    void test_setOverdraftLimit_should_configure_overdraft_successfully() throws Exception {
        // Given - Create an account
        String accountId = createAccount(AccountType.CURRENT, EUR);

        // When - Set overdraft limit
        SetOverdraftLimitRequest overdraftRequest = new SetOverdraftLimitRequest(new BigDecimal("100.00"));

        mockMvc.perform(put("/v1/accounts/" + accountId + "/overdraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overdraftRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.overdraftLimit").value(100.00))
                .andExpect(jsonPath("$.balance").value(0.00))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void test_setOverdraftLimit_should_allow_withdrawal_within_overdraft() throws Exception {
        // Given - Create account and set overdraft
        CreateAccountRequest createRequest = new CreateAccountRequest(AccountType.CURRENT, EUR);
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accountId = objectMapper.readTree(createResponse).get("accountId").asText();

        // Set overdraft
        mockMvc.perform(put("/v1/accounts/" + accountId + "/overdraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"overdraftLimit\": 100.00}"))
                .andExpect(status().isOk());

        // Deposit
        mockMvc.perform(post("/v1/accounts/" + accountId + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 50.00}"))
                .andExpect(status().isOk());

        // When - Withdraw using overdraft
        mockMvc.perform(post("/v1/accounts/" + accountId + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 120.00}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(-70.00))
                .andExpect(jsonPath("$.overdraftLimit").value(100.00));
    }

    @Test
    void test_setOverdraftLimit_should_update_existing_overdraft() throws Exception {
        // Given - Create account with overdraft
        CreateAccountRequest createRequest = new CreateAccountRequest(AccountType.CURRENT, EUR);
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accountId = objectMapper.readTree(createResponse).get("accountId").asText();

        // Set initial overdraft
        mockMvc.perform(put("/v1/accounts/" + accountId + "/overdraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"overdraftLimit\": 100.00}"))
                .andExpect(status().isOk());

        // When - Update overdraft
        mockMvc.perform(put("/v1/accounts/" + accountId + "/overdraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"overdraftLimit\": 200.00}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdraftLimit").value(200.00));
    }

    @Test
    void test_setOverdraftLimit_should_allow_zero() throws Exception {
        // Given
        CreateAccountRequest createRequest = new CreateAccountRequest(AccountType.CURRENT, EUR);
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accountId = objectMapper.readTree(createResponse).get("accountId").asText();

        // When - Set overdraft to zero
        mockMvc.perform(put("/v1/accounts/" + accountId + "/overdraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"overdraftLimit\": 0.00}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdraftLimit").value(0.00));
    }

    @Test
    void test_setOverdraftLimit_should_return_404_when_account_not_found() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(put("/v1/accounts/" + nonExistentId + "/overdraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"overdraftLimit\": 100.00}"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void test_setOverdraftLimit_should_return_400_for_savings_account() throws Exception {
        // Given - Create a SAVINGS account
        CreateAccountRequest createRequest = new CreateAccountRequest(AccountType.SAVINGS, EUR);
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accountId = objectMapper.readTree(createResponse).get("accountId").asText();

        // When & Then - Savings accounts don't support overdraft (UnsupportedOperationException)
        mockMvc.perform(put("/v1/accounts/" + accountId + "/overdraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"overdraftLimit\": 100.00}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Unsupported Operation"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Savings accounts cannot have overdraft authorization"));
    }

    @ParameterizedTest(name = "Invalid overdraft: {1}")
    @CsvSource({
            "-100.00, negative_overdraft",
            "null,    null_overdraft"
    })
    void test_setOverdraftLimit_should_return_400_for_invalid_values(String overdraftValue, String scenario) throws Exception {
        // Given
        CreateAccountRequest createRequest = new CreateAccountRequest(AccountType.CURRENT, EUR);
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accountId = objectMapper.readTree(createResponse).get("accountId").asText();

        // When & Then
        String requestBody = "{\"overdraftLimit\": " + overdraftValue + "}";
        mockMvc.perform(put("/v1/accounts/" + accountId + "/overdraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "Set overdraft to {0}")
    @CsvSource({
            "0.00,    zero",
            "50.00,   small",
            "100.00,  standard",
            "500.00,  large",
            "1000.00, premium"
    })
    void test_setOverdraftLimit_should_accept_various_valid_amounts(String overdraftAmount, String description) throws Exception {
        // Given
        CreateAccountRequest createRequest = new CreateAccountRequest(AccountType.CURRENT, EUR);
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accountId = objectMapper.readTree(createResponse).get("accountId").asText();

        // When
        String overdraftRequest = String.format("{\"overdraftLimit\": %s}", overdraftAmount);

        mockMvc.perform(put("/v1/accounts/" + accountId + "/overdraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(overdraftRequest))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overdraftLimit").value(Double.parseDouble(overdraftAmount)));
    }

    @Test
    void test_complete_scenario_with_overdraft_configuration() throws Exception {
        // Given - Create account
        CreateAccountRequest createRequest = new CreateAccountRequest(AccountType.CURRENT, USD);
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accountId = objectMapper.readTree(createResponse).get("accountId").asText();

        // When - Configure overdraft
        mockMvc.perform(put("/v1/accounts/" + accountId + "/overdraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"overdraftLimit\": 200.00}"))
                .andExpect(status().isOk());

        // Deposit
        mockMvc.perform(post("/v1/accounts/" + accountId + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100.00}"))
                .andExpect(status().isOk());

        // Withdraw using overdraft
        mockMvc.perform(post("/v1/accounts/" + accountId + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 250.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(-150.00));

        // Verify account state
        mockMvc.perform(get("/v1/accounts/" + accountId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(-150.00))
                .andExpect(jsonPath("$.overdraftLimit").value(200.00))
                .andExpect(jsonPath("$.currency").value("USD"));
    }
}
