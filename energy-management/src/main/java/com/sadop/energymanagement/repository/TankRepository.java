package com.sadop.energymanagement.repository;

import com.sadop.energymanagement.model.Tank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TankRepository extends JpaRepository<Tank, Long> {

    // Find tank by type globally (old usage)
    Optional<Tank> findByTankTypeIgnoreCase(String tankType);

    // Find tank by name (if needed)
    Optional<Tank> findByTankName(String tankName);

    // ✅ NEW: Find tank by type and branch
    Optional<Tank> findByTankTypeIgnoreCaseAndBranchId(String tankType, Long branchId);
}
