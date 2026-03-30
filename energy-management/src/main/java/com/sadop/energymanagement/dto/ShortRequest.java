package com.sadop.energymanagement.dto;

import java.math.BigDecimal;

public class ShortRequest {
    private Long attendantId;
    private BigDecimal amount;

    // Getters and Setters
    public Long getAttendantId() {
        return attendantId;
    }

    public void setAttendantId(Long attendantId) {
        this.attendantId = attendantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
