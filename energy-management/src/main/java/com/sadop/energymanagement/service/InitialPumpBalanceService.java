package com.sadop.energymanagement.service;

import com.sadop.energymanagement.model.InitialPumpBalance;
import com.sadop.energymanagement.repository.InitialPumpBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class InitialPumpBalanceService {

    @Autowired
    private InitialPumpBalanceRepository repository;

    public InitialPumpBalance save(InitialPumpBalance balance) {
        if (repository.findByPumpName(balance.getPumpName()).isPresent()) {
            throw new RuntimeException("Initial balance already exists for this pump.");
        }
        return repository.save(balance);
    }

    public Optional<Double> getOpeningBalance(String pumpName) {
        return repository.findByPumpName(pumpName).map(InitialPumpBalance::getOpeningBalance);
    }
}
