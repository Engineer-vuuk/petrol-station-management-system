package com.sadop.energymanagement.service;

import com.sadop.energymanagement.model.TankLevel;
import com.sadop.energymanagement.repository.TankLevelRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TankLevelService {

    private final TankLevelRepository tankLevelRepository;

    public TankLevel getTankLevelByFuelTypeAndBranch(String fuelType, Long branchId) {
        return tankLevelRepository.findByFuelTypeAndBranchId(fuelType, branchId)
                .orElseGet(() -> {
                    TankLevel newTank = TankLevel.builder()
                            .fuelType(fuelType)
                            .currentLevel(BigDecimal.ZERO)
                            .branchId(branchId)
                            .build();
                    return tankLevelRepository.save(newTank);
                });
    }

    @Transactional
    public void increaseTankLevel(String fuelType, BigDecimal amount, Long branchId) {
        TankLevel tank = getTankLevelByFuelTypeAndBranch(fuelType, branchId);
        BigDecimal updatedLevel = tank.getCurrentLevel().add(amount);
        tank.setCurrentLevel(updatedLevel);
        tankLevelRepository.save(tank);
    }

    @Transactional
    public boolean decreaseTankLevel(String fuelType, BigDecimal amount, Long branchId) {
        TankLevel tank = getTankLevelByFuelTypeAndBranch(fuelType, branchId);
        if (tank.getCurrentLevel().compareTo(amount) < 0) {
            return false; // Not enough fuel
        }
        BigDecimal updatedLevel = tank.getCurrentLevel().subtract(amount);
        tank.setCurrentLevel(updatedLevel);
        tankLevelRepository.save(tank);
        return true;
    }
}
