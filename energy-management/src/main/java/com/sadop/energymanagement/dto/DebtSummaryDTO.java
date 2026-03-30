package com.sadop.energymanagement.dto;

import java.math.BigDecimal;

public class DebtSummaryDTO {

    private BigDecimal totalOwed;
    private BigDecimal totalPaid;
    private BigDecimal totalRemaining;

    public DebtSummaryDTO(BigDecimal totalOwed, BigDecimal totalPaid, BigDecimal totalRemaining) {
        this.totalOwed = totalOwed;
        this.totalPaid = totalPaid;
        this.totalRemaining = totalRemaining;
    }

    public BigDecimal getTotalOwed() {
        return totalOwed;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public BigDecimal getTotalRemaining() {
        return totalRemaining;
    }
}
