package com.sadop.energymanagement.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Debt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Attendant ID cannot be null")
    @Column(nullable = false)
    private Long attendantId;

    @NotNull(message = "Debtor name cannot be null")
    @Column(nullable = false)
    private String debtorName;

    private String phone;

    @NotNull(message = "Amount owed cannot be null")
    @Column(nullable = false)
    private BigDecimal amountOwed;

    @Column(nullable = false)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false) // Debt belongs to one Branch
    @JsonBackReference("branch-debts")
    private Branch branch;


    // New field to track the latest paid amount
    @Column(nullable = false)
    private BigDecimal latestPaidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebtStatus status = DebtStatus.UNPAID;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_payment_mode")
    private String lastPaymentMode;

    // New field to track the date and time a payment was made
    private LocalDateTime paidAt;



    // ManyToOne relationship between Debt and User (Attendant)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-debts")
    private User user;

    @ManyToOne
    @JoinColumn(name = "sales_entry_id")
    @JsonBackReference("sales-entry-debts")
    private SalesEntry salesEntry;

    // Constructors
    public Debt() {
    }

    public Debt(Long attendantId, String debtorName, String phone, BigDecimal amountOwed, User user) {
        this.attendantId = attendantId;
        this.debtorName = debtorName;
        this.phone = phone;
        this.amountOwed = amountOwed;
        this.amountPaid = BigDecimal.ZERO;
        this.latestPaidAmount = BigDecimal.ZERO;
        this.status = DebtStatus.UNPAID;
        this.createdAt = LocalDateTime.now();
        this.user = user;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public Long getAttendantId() {
        return attendantId;
    }

    public void setAttendantId(Long attendantId) {
        this.attendantId = attendantId;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public void setDebtorName(String debtorName) {
        this.debtorName = debtorName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getAmountOwed() {
        return amountOwed;
    }

    public void setAmountOwed(BigDecimal amountOwed) {
        this.amountOwed = amountOwed;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getLatestPaidAmount() {
        return latestPaidAmount;
    }

    public void setLatestPaidAmount(BigDecimal latestPaidAmount) {
        this.latestPaidAmount = latestPaidAmount;
    }

    public DebtStatus getStatus() {
        return status;
    }

    public void setStatus(DebtStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public User getUser() {
        return user;
    }
    public String getLastPaymentMode() {
        return lastPaymentMode;
    }

    public void setLastPaymentMode(String lastPaymentMode) {
        this.lastPaymentMode = lastPaymentMode;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
