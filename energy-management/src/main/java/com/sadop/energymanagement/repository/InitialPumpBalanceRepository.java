package com.sadop.energymanagement.repository;

import com.sadop.energymanagement.model.InitialPumpBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface InitialPumpBalanceRepository extends JpaRepository<InitialPumpBalance, Long> {

    // Find InitialPumpBalance by pump name
    Optional<InitialPumpBalance> findByPumpName(String pumpName);

    // Find InitialPumpBalance by pump name and specific setup date
    Optional<InitialPumpBalance> findByPumpNameAndSetupDate(String pumpName, LocalDate setupDate);

    // ✅ Find the most recent InitialPumpBalance by pump name (by descending setup date)
    Optional<InitialPumpBalance> findTopByPumpNameOrderBySetupDateDesc(String pumpName);

}
