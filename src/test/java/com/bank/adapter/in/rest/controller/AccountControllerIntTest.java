package com.bank.adapter.in.rest.controller;

import com.bank.adapter.in.rest.request.CreateAccountRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Account Controller.
 * Tests ONLY the communication between layers: HTTP → Controller → Service → Repository → Database.
 *
 * IMPORTANT: Business logic (validation, exceptions, business rules) is fully tested in AccountServiceTest.
 * We do NOT retest them here.
 *
 * Purpose: Verify that all layers communicate correctly (HTTP, JSON, DB).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountControllerIntTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ========================================
    // INTEGRATION TESTS - ONE PER ENDPOINT
    // Each test is independent and tests ONLY its endpoint
    // ========================================

    @Test
    void test_createAccount_should_work_end_to_end() throws Exception {
        // Given
        CreateAccountRequest request = new CreateAccountRequest("USD");

        // When / Then - Verify HTTP → Controller → Service → Repository → DB
        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", matchesPattern("/v1/accounts/[0-9a-f-]{36}")))
                .andExpect(jsonPath("$.accountId").isNotEmpty())
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void test_getAccountDetails_should_work_end_to_end() throws Exception {
        // Given - Create an account first
        CreateAccountRequest createRequest = new CreateAccountRequest("EUR");
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accountId = objectMapper.readTree(createResponse).get("accountId").asText();

        // When / Then - Verify GET works
        mockMvc.perform(get("/v1/accounts/" + accountId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void test_deposit_should_work_end_to_end() throws Exception {
        // Given - Create an account first
        CreateAccountRequest createRequest = new CreateAccountRequest("EUR");
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accountId = objectMapper.readTree(createResponse).get("accountId").asText();

        // When / Then - Verify deposit works and returns updated account
        mockMvc.perform(post("/v1/accounts/" + accountId + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100.50}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.balance").value(100.50))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void test_withdraw_should_work_end_to_end() throws Exception {
        // Given - Create an account and deposit money first
        CreateAccountRequest createRequest = new CreateAccountRequest("EUR");
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accountId = objectMapper.readTree(createResponse).get("accountId").asText();

        // Deposit money first
        mockMvc.perform(post("/v1/accounts/" + accountId + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 200.00}"))
                .andExpect(status().isOk());

        // When / Then - Verify withdraw works and returns updated account
        mockMvc.perform(post("/v1/accounts/" + accountId + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 50.00}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.balance").value(150.00))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    // ========================================
    // ERROR HANDLING TESTS - ONE PER ENDPOINT
    // Verifies that exceptions are correctly propagated through HTTP layer
    // All business validation logic is tested in AccountServiceTest
    // ========================================

    @Test
    void test_createAccount_should_return_400_when_invalid_currency() throws Exception {
        // Given - Invalid currency format (triggers validation)
        CreateAccountRequest request = new CreateAccountRequest("invalid");

        // When / Then - Verify validation error is returned as 400
        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void test_getAccountDetails_should_return_404_when_account_not_found() throws Exception {
        // Given - Non-existent account ID
        String nonExistentAccountId = "550e8400-e29b-41d4-a716-446655440000";

        // When / Then - Verify 404 error is returned
        mockMvc.perform(get("/v1/accounts/" + nonExistentAccountId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void test_deposit_should_return_400_when_invalid_amount() throws Exception {
        // Given - Create an account first
        CreateAccountRequest createRequest = new CreateAccountRequest("EUR");
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accountId = objectMapper.readTree(createResponse).get("accountId").asText();

        // When / Then - Verify negative amount returns 400 (no account returned)
        mockMvc.perform(post("/v1/accounts/" + accountId + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": -50.00}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void test_withdraw_should_return_400_when_insufficient_funds() throws Exception {
        // Given - Create an account with small balance
        CreateAccountRequest createRequest = new CreateAccountRequest("EUR");
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accountId = objectMapper.readTree(createResponse).get("accountId").asText();

        // Deposit 50
        mockMvc.perform(post("/v1/accounts/" + accountId + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 50.00}"))
                .andExpect(status().isOk());

        // When / Then - Try to withdraw more than balance (insufficient funds, no account returned)
        mockMvc.perform(post("/v1/accounts/" + accountId + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100.00}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.status").value(400));
    }
}
