package com.sadop.energymanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShortSummary {
    private Long attendantId;
    private String attendantName;
    private BigDecimal totalShort;
    private String lastAction; // "ADD" or "REPAY"
    private LocalDateTime lastUpdated;

    // Getters and Setters
    public Long getAttendantId() {
        return attendantId;
    }

    public void setAttendantId(Long attendantId) {
        this.attendantId = attendantId;
    }

    public String getAttendantName() {
        return attendantName;
    }

    public void setAttendantName(String attendantName) {
        this.attendantName = attendantName;
    }

    public BigDecimal getTotalShort() {
        return totalShort;
    }

    public void setTotalShort(BigDecimal totalShort) {
        this.totalShort = totalShort;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
