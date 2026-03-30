package com.sadop.energymanagement.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "short_records") // Optional: explicit table name
public class ShortRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // ✅ Attendant relation
    @JoinColumn(name = "attendant_id", nullable = false)
    private User attendant;

    @ManyToOne(fetch = FetchType.LAZY) // ✅ New: Branch relation
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(nullable = false)
    private BigDecimal amount;

    private LocalDate date;

    private boolean submitted;

    private LocalDateTime lastUpdated;

    public enum Action {
        ADD, REPAY
    }

    @Enumerated(EnumType.STRING)
    private Action lastAction;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getAttendant() {
        return attendant;
    }

    public void setAttendant(User attendant) {
        this.attendant = attendant;
    }

    public Branch getBranch() { // ✅ new getter
        return branch;
    }

    public void setBranch(Branch branch) { // ✅ new setter
        this.branch = branch;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Action getLastAction() {
        return lastAction;
    }

    public void setLastAction(Action lastAction) {
        this.lastAction = lastAction;
    }
}
