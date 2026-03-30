package com.sadop.energymanagement.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tanks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tankType; // e.g., "Petrol", "Diesel"

    @Column(nullable = false)
    private BigDecimal currentLevel = BigDecimal.ZERO; // Current fuel level in litres

    @Column(nullable = false)
    private BigDecimal capacity = BigDecimal.ZERO; // Total tank capacity in litres

    private String tankName; // Optional descriptive name (if needed)

    @Column(nullable = false) // ✅ Added this field to support branch filtering
    private Long branchId;    // The ID of the branch this tank belongs to
}
