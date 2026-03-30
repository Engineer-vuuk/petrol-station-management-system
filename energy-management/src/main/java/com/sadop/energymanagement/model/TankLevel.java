package com.sadop.energymanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tank_levels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TankLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fuel_type", nullable = false, unique = true)
    private String fuelType; // "Petrol" or "Diesel"

    @Column(name = "current_level", nullable = false)
    private BigDecimal currentLevel; // in litres

    @Column(name = "tank_capacity", nullable = false)
    private BigDecimal tankCapacity; // in litres

    @Column(name = "branch_id", nullable = false) // ✅ Added field
    private Long branchId; // Each tank level belongs to a branch

    public void addFuel(BigDecimal quantity) {
        this.currentLevel = this.currentLevel.add(quantity);
        if (this.currentLevel.compareTo(tankCapacity) > 0) {
            this.currentLevel = tankCapacity;
        }
    }

    public void consumeFuel(BigDecimal quantity) {
        this.currentLevel = this.currentLevel.subtract(quantity);
        if (this.currentLevel.compareTo(BigDecimal.ZERO) < 0) {
            this.currentLevel = BigDecimal.ZERO;
        }
    }
}
