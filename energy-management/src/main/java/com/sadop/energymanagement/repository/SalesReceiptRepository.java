package com.sadop.energymanagement.repository;

import com.sadop.energymanagement.model.SalesReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SalesReceiptRepository extends JpaRepository<SalesReceipt, Long> {
    List<SalesReceipt> findByReceiptDate(LocalDate date);
}
