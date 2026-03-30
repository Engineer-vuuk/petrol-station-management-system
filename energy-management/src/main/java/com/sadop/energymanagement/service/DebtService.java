package com.sadop.energymanagement.service;

import com.sadop.energymanagement.dto.DebtSummaryDTO;
import com.sadop.energymanagement.model.*;
import com.sadop.energymanagement.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class DebtService {

    @Autowired
    private DebtRepository debtRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private HttpServletRequest request;

    public boolean isValidAttendant(Long attendantId) {
        Optional<User> userOptional = userRepository.findById(attendantId);
        return userOptional.filter(user -> user.getRole() == Role.ROLE_ATTENDANT).isPresent();
    }

    public void assignBranchToDebt(Debt debt, Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new IllegalArgumentException("Branch not found for ID: " + branchId));
        debt.setBranch(branch);
    }

    public Debt saveDebt(Debt debt) {
        if (!isValidAttendant(debt.getAttendantId())) {
            throw new IllegalArgumentException("Invalid attendant ID or attendant does not have the 'attendant' role");
        }

        User user = userRepository.findById(debt.getAttendantId())
                .orElseThrow(() -> new IllegalArgumentException("Attendant not found for attendant ID: " + debt.getAttendantId()));

        debt.setUser(user);
        return debtRepository.save(debt);
    }

    public Optional<Debt> payDebt(Long id, BigDecimal amount, String mode, Long branchId) {
        Optional<Debt> optionalDebt = debtRepository.findByIdAndBranchId(id, branchId);
        if (optionalDebt.isPresent()) {
            Debt debt = optionalDebt.get();

            BigDecimal remainingAmount = debt.getAmountOwed().subtract(debt.getAmountPaid());

            if (debt.getStatus() == DebtStatus.PARTIALLY_PAID) {
                if (amount.compareTo(remainingAmount) != 0) {
                    throw new IllegalArgumentException("You have already made a partial payment. You must now pay the full remaining balance of " + remainingAmount);
                }
            } else if (amount.compareTo(debt.getAmountOwed()) > 0) {
                throw new IllegalArgumentException("Payment exceeds the amount owed.");
            }

            BigDecimal newPaidAmount = debt.getAmountPaid().add(amount);
            debt.setAmountPaid(newPaidAmount);
            debt.setLatestPaidAmount(amount);
            debt.setPaidAt(LocalDateTime.now());
            debt.setLastPaymentMode(mode);

            if (newPaidAmount.compareTo(debt.getAmountOwed()) >= 0) {
                debt.setStatus(DebtStatus.PAID);
            } else {
                debt.setStatus(DebtStatus.PARTIALLY_PAID);
            }

            return Optional.of(debtRepository.save(debt));
        }

        return Optional.empty();
    }

    public void deleteDebtById(Long id) {
        debtRepository.deleteById(id);
    }

    public DebtSummaryDTO getDebtsSummaryByAttendantAndDate(Long attendantId, LocalDate date) {
        List<Debt> debts = debtRepository.findByAttendantIdAndCreatedAtBetween(attendantId, date.atStartOfDay(), date.atTime(LocalTime.MAX));
        BigDecimal totalOwed = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (Debt debt : debts) {
            totalOwed = totalOwed.add(debt.getAmountOwed());
            totalPaid = totalPaid.add(debt.getAmountPaid());
        }
        BigDecimal totalRemaining = totalOwed.subtract(totalPaid);
        return new DebtSummaryDTO(totalOwed, totalPaid, totalRemaining);
    }

    public List<Debt> getDebtsByStatusAndBranch(DebtStatus status, Long branchId) {
        return debtRepository.findByStatusAndBranchId(status, branchId);
    }

    public List<Debt> getDebtsByStatusDateAndBranch(DebtStatus status, LocalDate date, Long branchId) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return status == DebtStatus.PAID ?
                debtRepository.findByStatusAndPaidAtBetweenAndBranchId(status, start, end, branchId)
                : debtRepository.findByStatusAndCreatedAtBetweenAndBranchId(status, start, end, branchId);
    }

    public List<Debt> getDebtsByStatusesAndBranch(List<DebtStatus> statuses, Long branchId) {
        return debtRepository.findByStatusInAndBranchId(statuses, branchId);
    }

    public List<Debt> getAllDebtsByBranch(Long branchId) {
        return debtRepository.findByBranchId(branchId);
    }

    public Optional<Debt> getDebtByIdAndBranch(Long id, Long branchId) {
        return debtRepository.findByIdAndBranchId(id, branchId);
    }

    public Map<String, BigDecimal> calculateTotalsByBranch(Long branchId) {
        List<Debt> allDebts = debtRepository.findByBranchId(branchId);
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalOwed = BigDecimal.ZERO;
        BigDecimal totalRemaining = BigDecimal.ZERO;
        for (Debt debt : allDebts) {
            totalPaid = totalPaid.add(debt.getAmountPaid());
            totalOwed = totalOwed.add(debt.getAmountOwed());
            totalRemaining = totalRemaining.add(debt.getAmountOwed().subtract(debt.getAmountPaid()));
        }
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("totalPaid", totalPaid);
        result.put("totalOwed", totalOwed);
        result.put("totalRemaining", totalRemaining);
        return result;
    }

    public List<Debt> getDebtsPaidByAttendantOnDateAndBranch(Long attendantId, LocalDate date, Long branchId) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return debtRepository.findByAttendantIdAndPaidAtBetweenAndBranchId(attendantId, start, end, branchId)
                .stream().sorted(Comparator.comparing(Debt::getPaidAt)).toList();
    }

    public List<Debt> getDebtsPaidByAttendantOnDate(Long attendantId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return debtRepository.findByAttendantIdAndPaidAtBetween(attendantId, start, end)
                .stream().sorted(Comparator.comparing(Debt::getPaidAt)).toList();
    }

    public List<Debt> getDebtsByStatuses(List<DebtStatus> statuses) {
        return debtRepository.findByStatusIn(statuses);
    }

    public List<Debt> getAllDebts() {
        return debtRepository.findAll();
    }

    public Optional<Debt> getDebtById(Long id) {
        return debtRepository.findById(id);
    }

    public List<Debt> getDebtsByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return debtRepository.findByCreatedAtBetween(start, end);
    }

    public List<Debt> getDebtsByStatusAndDate(DebtStatus status, LocalDate date) {
        return status == DebtStatus.PAID ? getDebtsByStatusAndPaidDate(status, date)
                : debtRepository.findByStatusAndCreatedAtBetween(status, date.atStartOfDay(), date.atTime(LocalTime.MAX));
    }

    public List<Debt> getDebtsByStatusAndPaidDate(DebtStatus status, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return debtRepository.findByStatusAndPaidAtBetween(status, start, end);
    }
}
