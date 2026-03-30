package com.sadop.energymanagement.repository;

import com.sadop.energymanagement.model.FuelRestock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuelRestockRepository extends JpaRepository<FuelRestock, Long> {

    /**
     * Find all restocks by fuel type, ignoring case.
     */
    List<FuelRestock> findByFuelTypeIgnoreCase(String fuelType);

    /**
     * Find the most recent 10 restocks ordered by date descending.
     */
    List<FuelRestock> findTop10ByOrderByRestockDateDesc();

    /**
     * ✅ New: Find restocks for a specific branch.
     */
    List<FuelRestock> findByBranchId(Long branchId);

    /**
     * ✅ New: Find recent 10 restocks for a specific branch.
     */
    List<FuelRestock> findTop10ByBranchIdOrderByRestockDateDesc(Long branchId);

    /**
     * ✅ New: Find restocks by fuel type and branch.
     */
    List<FuelRestock> findByFuelTypeIgnoreCaseAndBranchId(String fuelType, Long branchId);
}
