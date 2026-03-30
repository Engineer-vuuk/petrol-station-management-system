package com.sadop.energymanagement.repository;

import com.sadop.energymanagement.model.Expense;
import com.sadop.energymanagement.model.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByDate(LocalDate date);

    List<Expense> findByStatus(ExpenseStatus status);

    // ✅ New methods for branch functionality
    List<Expense> findByDateAndBranchId(LocalDate date, Long branchId);

    List<Expense> findByStatusAndBranchId(ExpenseStatus status, Long branchId);

    List<Expense> findByBranchId(Long branchId);
}
