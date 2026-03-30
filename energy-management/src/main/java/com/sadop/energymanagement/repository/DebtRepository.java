package com.sadop.energymanagement.repository;

import com.sadop.energymanagement.model.Debt;
import com.sadop.energymanagement.model.DebtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DebtRepository extends JpaRepository<Debt, Long> {

    // Retrieve debts by status
    List<Debt> findByStatus(DebtStatus status);

    // Retrieve debts created between the specified time range (using LocalDateTime)
    List<Debt> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Retrieve debts by status and created time range
    List<Debt> findByStatusAndCreatedAtBetween(DebtStatus status, LocalDateTime start, LocalDateTime end);

    // Retrieve debts by attendantId, status, and created time range
    List<Debt> findByAttendantIdAndStatusAndCreatedAtBetween(Long attendantId, DebtStatus status, LocalDateTime start, LocalDateTime end);

    // Retrieve debts by attendantId and created time range (without status)
    List<Debt> findByAttendantIdAndCreatedAtBetween(Long attendantId, LocalDateTime start, LocalDateTime end);

    // Retrieve debts by multiple statuses
    List<Debt> findByStatusIn(List<DebtStatus> statuses);

    // Retrieve debts by status and paidAt time range
    List<Debt> findByStatusAndPaidAtBetween(DebtStatus status, LocalDateTime start, LocalDateTime end);

    // Retrieve debts by attendantId and paidAt time range
    List<Debt> findByAttendantIdAndPaidAtBetween(Long attendantId, LocalDateTime start, LocalDateTime end);

    // ✅ Branch-aware methods added below:

    // Retrieve debts by status and branch
    List<Debt> findByStatusAndBranchId(DebtStatus status, Long branchId);

    // Retrieve debts by status, created time range, and branch
    List<Debt> findByStatusAndCreatedAtBetweenAndBranchId(DebtStatus status, LocalDateTime start, LocalDateTime end, Long branchId);

    // Retrieve debts by status, paidAt time range, and branch
    List<Debt> findByStatusAndPaidAtBetweenAndBranchId(DebtStatus status, LocalDateTime start, LocalDateTime end, Long branchId);

    // Retrieve debts by multiple statuses and branch
    List<Debt> findByStatusInAndBranchId(List<DebtStatus> statuses, Long branchId);

    // Retrieve all debts in a specific branch
    List<Debt> findByBranchId(Long branchId);

    // Retrieve a specific debt by ID and branch
    Optional<Debt> findByIdAndBranchId(Long id, Long branchId);

    // Retrieve debts paid by attendant on date in a branch
    List<Debt> findByAttendantIdAndPaidAtBetweenAndBranchId(Long attendantId, LocalDateTime start, LocalDateTime end, Long branchId);
}
