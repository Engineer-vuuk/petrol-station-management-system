package com.sadop.energymanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class SalesReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate receiptDate;
    private String managerId;

    private double totalPetrol;       // petrol1 + petrol2
    private double totalDiesel;       // diesel1 + diesel2

    private double totalCash;
    private double totalMpesa;
    private double totalDebts;
    private double paidDebts;
    private double expectedCash;
    private double totalPumpSales;
    private double shortOrLoss;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "receipt_id") // foreign key in sales_entry table
    private List<SalesEntry> submittedEntries;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(LocalDate receiptDate) {
        this.receiptDate = receiptDate;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public double getTotalPetrol() {
        return totalPetrol;
    }

    public void setTotalPetrol(double totalPetrol) {
        this.totalPetrol = totalPetrol;
    }

    public double getTotalDiesel() {
        return totalDiesel;
    }

    public void setTotalDiesel(double totalDiesel) {
        this.totalDiesel = totalDiesel;
    }

    public double getTotalCash() {
        return totalCash;
    }

    public void setTotalCash(double totalCash) {
        this.totalCash = totalCash;
    }

    public double getTotalMpesa() {
        return totalMpesa;
    }

    public void setTotalMpesa(double totalMpesa) {
        this.totalMpesa = totalMpesa;
    }

    public double getTotalDebts() {
        return totalDebts;
    }

    public void setTotalDebts(double totalDebts) {
        this.totalDebts = totalDebts;
    }

    public double getPaidDebts() {
        return paidDebts;
    }

    public void setPaidDebts(double paidDebts) {
        this.paidDebts = paidDebts;
    }

    public double getExpectedCash() {
        return expectedCash;
    }

    public void setExpectedCash(double expectedCash) {
        this.expectedCash = expectedCash;
    }

    public double getTotalPumpSales() {
        return totalPumpSales;
    }

    public void setTotalPumpSales(double totalPumpSales) {
        this.totalPumpSales = totalPumpSales;
    }

    public double getShortOrLoss() {
        return shortOrLoss;
    }

    public void setShortOrLoss(double shortOrLoss) {
        this.shortOrLoss = shortOrLoss;
    }

    public List<SalesEntry> getSubmittedEntries() {
        return submittedEntries;
    }

    public void setSubmittedEntries(List<SalesEntry> submittedEntries) {
        this.submittedEntries = submittedEntries;
    }
}
