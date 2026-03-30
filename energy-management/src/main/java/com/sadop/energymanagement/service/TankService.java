package com.sadop.energymanagement.service;

import com.sadop.energymanagement.model.Tank;
import com.sadop.energymanagement.repository.TankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TankService {

    @Autowired
    private TankRepository tankRepository;

    public List<Tank> getAllTanks() {
        return tankRepository.findAll();
    }

    public BigDecimal getTotalFuelByType(String type) {
        return tankRepository.findByTankTypeIgnoreCase(type)
                .map(Tank::getCurrentLevel)
                .orElse(BigDecimal.ZERO);
    }

    public BigDecimal getTotalFuelByTypeAndBranch(String type, Long branchId) {
        return tankRepository.findByTankTypeIgnoreCaseAndBranchId(type, branchId)
                .map(Tank::getCurrentLevel)
                .orElse(BigDecimal.ZERO);
    }

    public void addFuelToTank(String tankType, BigDecimal amount, Long branchId) {
        Tank tank = tankRepository.findByTankTypeIgnoreCaseAndBranchId(tankType, branchId)
                .orElseThrow(() -> new RuntimeException("Tank not found for type: " + tankType + " in branch: " + branchId));

        tank.setCurrentLevel(tank.getCurrentLevel().add(amount));
        tankRepository.save(tank);
    }

    public void subtractFuelFromTank(String tankType, BigDecimal amount, Long branchId) {
        Tank tank = tankRepository.findByTankTypeIgnoreCaseAndBranchId(tankType, branchId)
                .orElseThrow(() -> new RuntimeException("Tank not found for type: " + tankType + " in branch: " + branchId));

        if (tank.getCurrentLevel().compareTo(amount) < 0) {
            throw new RuntimeException("Not enough fuel in " + tankType + " tank for branch: " + branchId);
        }

        tank.setCurrentLevel(tank.getCurrentLevel().subtract(amount));
        tankRepository.save(tank);
    }

    public Optional<Tank> getTankByPumpNameAndBranch(String pumpName, Long branchId) {
        if (pumpName == null) return Optional.empty();

        String tankType = extractTankType(pumpName);
        if (tankType == null) return Optional.empty();

        return tankRepository.findByTankTypeIgnoreCaseAndBranchId(tankType, branchId);
    }

    public Optional<Tank> getTankByPumpName(String pumpName) {
        if (pumpName == null) return Optional.empty();

        String tankType = extractTankType(pumpName);
        if (tankType == null) return Optional.empty();

        return tankRepository.findByTankTypeIgnoreCase(tankType);
    }

    public boolean hasSufficientFuel(String tankType, BigDecimal amount, Long branchId) {
        return tankRepository.findByTankTypeIgnoreCaseAndBranchId(tankType, branchId)
                .map(tank -> tank.getCurrentLevel().compareTo(amount) >= 0)
                .orElse(false);
    }

    public double getFuelLevelByPumpNameAndBranch(String pumpName, Long branchId) {
        return getTankByPumpNameAndBranch(pumpName, branchId)
                .map(tank -> tank.getCurrentLevel().doubleValue())
                .orElseThrow(() -> new RuntimeException("Tank not found for pump: " + pumpName + " in branch: " + branchId));
    }

    public void subtractFuelFromTankByPumpAndBranch(String pumpName, BigDecimal amount, Long branchId) {
        Tank tank = getTankByPumpNameAndBranch(pumpName, branchId)
                .orElseThrow(() -> new RuntimeException("No tank found for pump: " + pumpName + " in branch: " + branchId));

        if (tank.getCurrentLevel().compareTo(amount) < 0) {
            throw new RuntimeException("Not enough fuel in " + tank.getTankType() + " tank for pump: " + pumpName + " in branch: " + branchId);
        }

        tank.setCurrentLevel(tank.getCurrentLevel().subtract(amount));
        tankRepository.save(tank);
    }

    private String extractTankType(String pumpName) {
        pumpName = pumpName.toLowerCase();
        if (pumpName.contains("petrol")) return "petrol";
        if (pumpName.contains("diesel")) return "diesel";
        return null;
    }
}
