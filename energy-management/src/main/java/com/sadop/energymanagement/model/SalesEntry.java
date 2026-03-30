package com.sadop.energymanagement.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate salesDate;
    private String pumpName;
    private String attendantId;

    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal pricePerLitre;

    // ✅ New fields for buying price and profit
    private BigDecimal buyingPrice;

    @Builder.Default
    @Column(nullable = false)
    private BigDecimal profit = BigDecimal.ZERO;

    private BigDecimal totalCash;
    private BigDecimal totalMpesa;

    @Builder.Default
    @Column(name = "equitel", nullable = false)
    private BigDecimal equitel = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "family_bank", nullable = false)
    private BigDecimal familyBank = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "visa_card", nullable = false)
    private BigDecimal visaCard = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "other_mobile_money", nullable = false)
    private BigDecimal otherMobileMoney = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "credit_sales", nullable = false)
    private BigDecimal creditSales = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_debts", nullable = false)
    private BigDecimal totalDebts = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "discount", nullable = false)
    private BigDecimal discount = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "paid_debts", nullable = false)
    private BigDecimal paidDebts = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_expenses", nullable = false)
    private BigDecimal totalExpenses = BigDecimal.ZERO;

    private BigDecimal fuelConsumed;
    private BigDecimal expectedCash;
    private BigDecimal shortOrLoss;
    private BigDecimal totalPumpSales;

    private String remarks;
    private String status;

    @Column(nullable = false, unique = true)
    private String entryNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "salesEntry", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("sales-entry-debts")
    private List<Debt> debts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = "draft";
        }
        if (entryNumber == null) {
            entryNumber = "ENT-" + System.currentTimeMillis();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (submittedAt == null && !"draft".equalsIgnoreCase(status)) {
            submittedAt = LocalDateTime.now();
        }

        // ✅ Calculate profit if all necessary fields are present
        calculateProfit();
    }

    @PreUpdate
    public void preUpdate() {
        calculateProfit();
    }

    public void calculateProfit() {
        if (pricePerLitre != null && buyingPrice != null && fuelConsumed != null) {
            this.profit = pricePerLitre.subtract(buyingPrice).multiply(fuelConsumed);
        } else {
            this.profit = BigDecimal.ZERO;
        }
    }

    public void calculateTotalDebts() {
        this.totalDebts = (debts != null && !debts.isEmpty())
                ? debts.stream()
                .map(Debt::getAmountOwed)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;
    }

    public void addToTotalCash(double amount) {
        if (this.totalCash == null) {
            this.totalCash = BigDecimal.ZERO;
        }
        this.totalCash = this.totalCash.add(BigDecimal.valueOf(amount));
    }

    public void addToTotalMpesa(double amount) {
        if (this.totalMpesa == null) {
            this.totalMpesa = BigDecimal.ZERO;
        }
        this.totalMpesa = this.totalMpesa.add(BigDecimal.valueOf(amount));
    }

    public void addToOpeningBalance(double amount) {
        if (this.openingBalance == null) {
            this.openingBalance = BigDecimal.ZERO;
        }
        this.openingBalance = this.openingBalance.add(BigDecimal.valueOf(amount));
    }

    public void addToClosingBalance(double amount) {
        if (this.closingBalance == null) {
            this.closingBalance = BigDecimal.ZERO;
        }
        this.closingBalance = this.closingBalance.add(BigDecimal.valueOf(amount));
    }
}
