package com.sadop.energymanagement.repository;

import com.sadop.energymanagement.model.TankLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TankLevelRepository extends JpaRepository<TankLevel, Long> {

    // Old method (if you still want it)
    Optional<TankLevel> findByFuelType(String fuelType);

    // ✅ New: Find by fuel type and branch
    Optional<TankLevel> findByFuelTypeAndBranchId(String fuelType, Long branchId);
}
