package com.sadop.energymanagement.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;

@Entity
public class InitialPumpBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pumpName;

    private Double openingBalance;

    private LocalDate setupDate; // optional

    // ✅ Constructor
    public InitialPumpBalance() {}

    public InitialPumpBalance(String pumpName, Double openingBalance, LocalDate setupDate) {
        this.pumpName = pumpName;
        this.openingBalance = openingBalance;
        this.setupDate = setupDate;
    }

    // ✅ Getters
    public Long getId() {
        return id;
    }

    public String getPumpName() {
        return pumpName;
    }

    public Double getOpeningBalance() {
        return openingBalance;
    }

    public LocalDate getSetupDate() {
        return setupDate;
    }

    // ✅ Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setPumpName(String pumpName) {
        this.pumpName = pumpName;
    }

    public void setOpeningBalance(Double openingBalance) {
        this.openingBalance = openingBalance;
    }

    public void setSetupDate(LocalDate setupDate) {
        this.setupDate = setupDate;
    }
}
