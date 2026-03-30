package com.sadop.energymanagement.service;

import com.sadop.energymanagement.model.FuelRestock;
import com.sadop.energymanagement.repository.FuelRestockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FuelRestockService {

    @Autowired
    private FuelRestockRepository fuelRestockRepository;

    @Autowired
    private TankService tankService;

    /**
     * Handles saving the restock and updating the tank for a specific branch.
     */
    public FuelRestock restockFuel(FuelRestock restock) {
        restock.setRestockDate(LocalDateTime.now());

        FuelRestock savedRestock = fuelRestockRepository.save(restock);

        if (savedRestock.getBranch() == null || savedRestock.getBranch().getId() == null) {
            throw new IllegalArgumentException("Branch must not be null when restocking fuel.");
        }

        Long branchId = savedRestock.getBranch().getId();

        tankService.addFuelToTank(savedRestock.getFuelType(), savedRestock.getQuantity(), branchId);

        return savedRestock;
    }

    public List<FuelRestock> getAllRestocks() {
        return fuelRestockRepository.findAll();
    }

    public List<FuelRestock> getAllRestocksByBranch(Long branchId) {
        return fuelRestockRepository.findByBranchId(branchId);
    }

    public List<FuelRestock> getRestocksByFuelType(String fuelType) {
        return fuelRestockRepository.findByFuelTypeIgnoreCase(fuelType);
    }

    public List<FuelRestock> getRestocksByFuelTypeAndBranch(String fuelType, Long branchId) {
        return fuelRestockRepository.findByFuelTypeIgnoreCaseAndBranchId(fuelType, branchId);
    }

    public List<FuelRestock> getRecentFuelRestocks() {
        return fuelRestockRepository.findTop10ByOrderByRestockDateDesc();
    }

    public List<FuelRestock> getRecentFuelRestocksByBranch(Long branchId) {
        return fuelRestockRepository.findTop10ByBranchIdOrderByRestockDateDesc(branchId);
    }
}
