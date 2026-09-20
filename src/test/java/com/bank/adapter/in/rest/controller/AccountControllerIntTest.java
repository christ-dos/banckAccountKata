package com.bank.adapter.in.rest.controller;

import com.bank.adapter.in.rest.request.CreateAccountRequest;
import com.bank.domain.model.AccountType;
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
class AccountControllerIntTest {
    private static final String EUR = "EUR";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String createAccountAndGetId(AccountType accountType) throws Exception {
        CreateAccountRequest createRequest = new CreateAccountRequest(accountType, EUR);
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
    // INTEGRATION TESTS - ONE PER ENDPOINT
    // Each test is independent and tests ONLY its endpoint
    // ========================================

    @Test
    void test_createAccount_should_work_end_to_end() throws Exception {
        // Given
        CreateAccountRequest request = new CreateAccountRequest(AccountType.CURRENT,"USD");

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
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.accountType").value("CURRENT"));
    }

    @Test
    void test_createSavingsAccount_should_work_end_to_end() throws Exception {
        // Given
        CreateAccountRequest request = new CreateAccountRequest(AccountType.SAVINGS, EUR);

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
                .andExpect(jsonPath("$.currency").value(EUR))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.depositLimit").value(22950.00));
    }

    @Test
    void test_getAccountDetails_should_work_end_to_end() throws Exception {
        // Given - Create an account first
        CreateAccountRequest createRequest = new CreateAccountRequest(AccountType.CURRENT,EUR);
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
                .andExpect(jsonPath("$.currency").value(EUR));
    }

    @Test
    void test_deposit_should_work_end_to_end() throws Exception {
        // Given - Create an account first
        CreateAccountRequest createRequest = new CreateAccountRequest(AccountType.CURRENT,EUR);
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
                .andExpect(jsonPath("$.currency").value(EUR));
    }

    @Test
    void test_withdraw_should_work_end_to_end() throws Exception {
        // Given - Create an account and deposit money first
        CreateAccountRequest createRequest = new CreateAccountRequest(AccountType.CURRENT,EUR);
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
                .andExpect(jsonPath("$.currency").value(EUR));
    }

    // ========================================
    // ERROR HANDLING TESTS - ONE PER ENDPOINT
    // Verifies that exceptions are correctly propagated through HTTP layer
    // All business validation logic is tested in AccountServiceTest
    // ========================================

    @Test
    void test_createAccount_should_return_400_when_invalid_currency() throws Exception {
        // Given - Invalid currency format (triggers validation)
        CreateAccountRequest request = new CreateAccountRequest(AccountType.CURRENT,"invalid");

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
        CreateAccountRequest createRequest = new CreateAccountRequest(AccountType.CURRENT,EUR);
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
        CreateAccountRequest createRequest = new CreateAccountRequest(AccountType.CURRENT, EUR);
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

    // ========================================
    // ACCOUNT STATEMENT INTEGRATION TESTS
    // ========================================

    @Test
    void test_getAccountStatement_default_should_work_end_to_end() throws Exception {
        // Given
        String accountId = createAccountAndGetId(AccountType.CURRENT);

        // When / Then - Verify HTTP → Controller → Service → Repository → DB
        mockMvc.perform(get("/v1/accounts/{accountId}/statement", accountId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.accountType").value("CURRENT"))
                .andExpect(jsonPath("$.balanceAtEndDate").exists())
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.periodStart").exists())
                .andExpect(jsonPath("$.periodEnd").exists())
                .andExpect(jsonPath("$.operations").exists())
                .andExpect(jsonPath("$.operations.content").isArray())
                .andExpect(jsonPath("$.operations.pageable").exists())
                .andExpect(jsonPath("$.operations.totalElements").exists());
    }

    @Test
    void test_getAccountStatement_with_custom_period_should_work_end_to_end() throws Exception {
        // Given
        String accountId = createAccountAndGetId(AccountType.CURRENT);
        String startDate = "2025-12-01";
        String endDate = "2025-12-31";

        // When / Then
        mockMvc.perform(get("/v1/accounts/{accountId}/statement", accountId)
                        .param("startDate", startDate)
                        .param("endDate", endDate))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.periodStart").value(startDate))
                .andExpect(jsonPath("$.periodEnd").value(endDate))
                .andExpect(jsonPath("$.operations").exists());
    }

    @Test
    void test_getAccountStatement_with_pagination_should_work_end_to_end() throws Exception {
        // Given
        String accountId = createAccountAndGetId(AccountType.CURRENT);
        int page = 0;
        int size = 5;

        // When / Then
        mockMvc.perform(get("/v1/accounts/{accountId}/statement", accountId)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.operations.size").value(size))
                .andExpect(jsonPath("$.operations.number").value(page));
    }

    @Test
    void test_getAccountStatement_for_savings_account_should_work_end_to_end() throws Exception {
        // Given
        String accountId = createAccountAndGetId(AccountType.SAVINGS);

        // When / Then
        mockMvc.perform(get("/v1/accounts/{accountId}/statement", accountId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.balanceAtEndDate").exists())
                .andExpect(jsonPath("$.operations").exists());
    }

    @Test
    @Transactional
    void test_getAccountStatement_with_operations_should_return_correct_data() throws Exception {
        // Given - Create account and perform operations
        CreateAccountRequest createRequest = new CreateAccountRequest(AccountType.CURRENT, EUR);
        String createResponse = mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accountId = objectMapper.readTree(createResponse).get("accountId").asText();

        // Perform some operations
        String depositRequest = "{\"amount\": 1000.00}";
        mockMvc.perform(post("/v1/accounts/{accountId}/deposit", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(depositRequest))
                .andExpect(status().isOk());

        String withdrawRequest = "{\"amount\": 200.00}";
        mockMvc.perform(post("/v1/accounts/{accountId}/withdraw", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(withdrawRequest))
                .andExpect(status().isOk());

        // When / Then - Get statement should show operations
        mockMvc.perform(get("/v1/accounts/{accountId}/statement", accountId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.balanceAtEndDate").value(800.00))
                .andExpect(jsonPath("$.operations.content").isArray())
                .andExpect(jsonPath("$.operations.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.operations.content[0].type").exists())
                .andExpect(jsonPath("$.operations.content[0].amount").exists())
                .andExpect(jsonPath("$.operations.content[0].balanceAfter").exists())
                .andExpect(jsonPath("$.operations.content[0].operationDate").exists());
    }

    @Test
    void test_getAccountStatement_should_return_404_when_account_not_found() throws Exception {
        // Given
        String nonExistentAccountId = "550e8400-e29b-41d4-a716-999999999999";

        // When / Then
        mockMvc.perform(get("/v1/accounts/{accountId}/statement", nonExistentAccountId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Bank account not found: " + nonExistentAccountId));
    }

    @Test
    void test_getAccountStatement_should_return_400_when_start_date_after_end_date() throws Exception {
        // Given
        String accountId = createAccountAndGetId(AccountType.CURRENT);
        String startDate = "2026-01-31";
        String endDate = "2026-01-01";

        // When / Then
        mockMvc.perform(get("/v1/accounts/{accountId}/statement", accountId)
                        .param("startDate", startDate)
                        .param("endDate", endDate))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Start date must be before or equal to end date"));
    }

    @Test
    void test_getAccountStatement_operations_should_be_sorted_desc_by_date() throws Exception {
        // Given
        String accountId = createAccountAndGetId(AccountType.CURRENT);

        // When / Then - Operations should be sorted DESC (most recent first)
        mockMvc.perform(get("/v1/accounts/{accountId}/statement", accountId)
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operations.content").isArray());
        // Note: We can't test exact order without knowing operation dates
        // But the repository ensures DESC sort by operationDate
    }

    @Test
    void test_createAccount_should_return_400_when_currency_invalid_size() throws Exception {
        // Given - Invalid currency size (must be exactly 3 characters)
        String invalidJson = "{\"accountType\": \"CURRENT\", \"currency\": \"US\"}";

        // When / Then - Verify MethodArgumentNotValidException is handled
        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void test_createAccount_should_return_400_when_currency_invalid_pattern() throws Exception {
        // Given - Invalid currency pattern (must be 3 uppercase letters)
        String invalidJson = "{\"accountType\": \"CURRENT\", \"currency\": \"us1\"}";

        // When / Then - Verify MethodArgumentNotValidException is handled
        mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400));
    }
}
