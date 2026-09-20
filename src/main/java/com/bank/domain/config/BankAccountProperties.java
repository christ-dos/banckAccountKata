package com.bank.domain.config;

import java.math.BigDecimal;

/**
 * Configuration properties for bank accounts business rules.
 * This is a pure domain configuration (no external dependencies like Lombok).
 * The adapter layer will extend this class and add Spring annotations.
 */
public abstract class BankAccountProperties {

    private SavingsAccountProperties savings = new SavingsAccountProperties();

    public SavingsAccountProperties getSavings() {
        return savings;
    }

    public void setSavings(SavingsAccountProperties savings) {
        this.savings = savings;
    }

    /**
     * Inner class for savings account specific properties.
     * Public to allow access to all savings-related properties.
     */
    public static class SavingsAccountProperties {
        /**
         * Default deposit limit for savings accounts (e.g., Livret A).
         * Default value: 22,950 EUR (fallback if not configured in application.yml)
         */
        private BigDecimal defaultDepositLimit = BigDecimal.valueOf(22950);

        public BigDecimal getDefaultDepositLimit() {
            return defaultDepositLimit;
        }

        public void setDefaultDepositLimit(BigDecimal defaultDepositLimit) {
            this.defaultDepositLimit = defaultDepositLimit;
        }
    }
}
