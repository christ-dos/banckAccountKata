package com.bank.adapter.in.rest.api;

import com.bank.adapter.in.rest.dto.AccountDto;
import com.bank.adapter.in.rest.request.CreateAccountRequest;
import com.bank.adapter.in.rest.request.DepositRequest;
import com.bank.adapter.in.rest.request.WithdrawRequest;
import com.bank.adapter.in.rest.request.SetOverdraftLimitRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

/**
 * API documentation interface for Bank Account Controller.
 * Separates OpenAPI documentation from controller implementation for better readability.
 */
@Tag(name = "Bank Accounts", description = "API for managing bank accounts")
public interface ApiDocAccountController {

    @Operation(
            summary = "Create a new bank account",
            description = "Creates a new bank account with the specified currency. If currency is not provided, EUR will be used as default."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully",
                    content = @Content(schema = @Schema(implementation = AccountDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request - currency format is incorrect"
            )
    })
    ResponseEntity<AccountDto> createAccount(@Valid @RequestBody CreateAccountRequest request);

    @Operation(
            summary = "Deposit money into an account",
            description = "Deposits a specified amount of money into the account. Amount must be positive. Returns the updated account with new balance."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Deposit successful - returns updated account",
                    content = @Content(schema = @Schema(implementation = AccountDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid amount - must be positive"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    ResponseEntity<AccountDto> deposit(@PathVariable UUID accountId, @Valid @RequestBody DepositRequest request);

    @Operation(
            summary = "Get account details",
            description = "Retrieves the details of a bank account including balance and currency."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account found",
                    content = @Content(schema = @Schema(implementation = AccountDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    ResponseEntity<AccountDto> getAccountDetails(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId
    );

    @Operation(
            summary = "Withdraw money from an account",
            description = "Withdraws a specified amount of money from the account. Amount must be positive and sufficient balance required. Returns the updated account with new balance."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Withdrawal successful - returns updated account",
                    content = @Content(schema = @Schema(implementation = AccountDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid amount or insufficient funds"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    ResponseEntity<AccountDto> withdraw(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId,
            @Valid @RequestBody WithdrawRequest request
    );

    @Operation(
            summary = "Configure overdraft limit",
            description = "Sets or updates the authorized overdraft limit for an account. The limit must be positive or zero. Returns the updated account."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Overdraft limit configured successfully - returns updated account",
                    content = @Content(schema = @Schema(implementation = AccountDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid overdraft limit - must be positive or zero"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found"
            )
    })
    ResponseEntity<AccountDto> setOverdraftLimit(
            @Parameter(description = "Account ID", required = true)
            @PathVariable UUID accountId,
            @Valid @RequestBody SetOverdraftLimitRequest request
    );
}
