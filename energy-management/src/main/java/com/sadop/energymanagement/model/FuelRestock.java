package com.sadop.energymanagement.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fuel_restocks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelRestock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fuelType; // e.g. "Petrol", "Diesel"

    @Column(nullable = false)
    private BigDecimal quantity; // Amount of fuel restocked in litres

    private String supplier;

    private String deliveryNote;

    @Column(name = "restock_date", nullable = false)
    private LocalDateTime restockDate;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    @ManyToOne(fetch = FetchType.LAZY) // Lazy loaded for performance
    @JoinColumn(name = "branch_id", nullable = false)
    @JsonIgnore // ✅ Prevent LazyInitializationException during JSON serialization
    private Branch branch;

    @PrePersist
    public void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
        if (restockDate == null) {
            restockDate = LocalDateTime.now();
        }
    }
}
