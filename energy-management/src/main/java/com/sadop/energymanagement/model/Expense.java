package com.sadop.energymanagement.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private double amount;

    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private ExpenseStatus status = ExpenseStatus.DRAFT;  // Default to DRAFT

    @Column(nullable = false)
    private Boolean submitted = false;  // Default to false

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;  // Automatically set when the record is inserted

    // ✅ NEW: ManyToOne relationship to Branch
    @ManyToOne
    @JoinColumn(name = "branch_id")
    private Branch branch;

    public Expense() {
    }

    public Expense(String type, double amount, LocalDate date, ExpenseStatus status) {
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.status = status != null ? status : ExpenseStatus.DRAFT;
        this.submitted = false; // Set default value to false
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public ExpenseStatus getStatus() {
        return status;
    }

    public void setStatus(ExpenseStatus status) {
        this.status = status;
    }

    public Boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ✅ NEW: Branch Getters and Setters
    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }
}
