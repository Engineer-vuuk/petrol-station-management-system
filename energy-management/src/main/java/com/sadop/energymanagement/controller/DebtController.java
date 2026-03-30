package com.sadop.energymanagement.controller;

import com.sadop.energymanagement.model.Debt;
import com.sadop.energymanagement.model.DebtStatus;
import com.sadop.energymanagement.model.Role;
import com.sadop.energymanagement.security.CustomUserDetails;
import com.sadop.energymanagement.service.AuditService;
import com.sadop.energymanagement.service.DebtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/debts")
public class DebtController {

    @Autowired private DebtService debtService;
    @Autowired private AuditService auditService;

    private Long getCurrentBranchId(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (userDetails.getUser().getRole() == Role.ROLE_CEO) {
            String branchIdHeader = request.getHeader("X-Branch-Id");
            if (branchIdHeader != null) {
                return Long.parseLong(branchIdHeader);
            } else {
                throw new RuntimeException("Missing X-Branch-Id header for CEO.");
            }
        }

        if (userDetails.getUser().getBranch() != null) {
            return userDetails.getUser().getBranch().getId();
        } else {
            throw new RuntimeException("User has no branch assigned.");
        }
    }

    private void log(String username, String action, String description, String entityId) {
        auditService.logActivity(username, action, description, "Debt", entityId);
    }

    // ✅ POST: Create Debt
    @PostMapping
    public ResponseEntity<Debt> createDebt(@Valid @RequestBody Debt debt, HttpServletRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long branchId = getCurrentBranchId(request);
        debt.setStatus(DebtStatus.UNPAID);
        debtService.assignBranchToDebt(debt, branchId);
        Debt saved = debtService.saveDebt(debt);

        log(userDetails.getUsername(), "CREATE_DEBT", "Created debt for " + debt.getDebtorName(), String.valueOf(saved.getId()));
        return ResponseEntity.ok(saved);
    }

    // ❌ GET: No logging
    @GetMapping("/status")
    public ResponseEntity<List<Debt>> getDebtsByStatus(@RequestParam DebtStatus status, HttpServletRequest request) {
        Long branchId = getCurrentBranchId(request);
        return ResponseEntity.ok(debtService.getDebtsByStatusAndBranch(status, branchId));
    }

    // ❌ GET: No logging
    @GetMapping("/daily")
    public ResponseEntity<List<Debt>> getDebtsByStatusAndDate(
            @RequestParam DebtStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request
    ) {
        Long branchId = getCurrentBranchId(request);
        return ResponseEntity.ok(debtService.getDebtsByStatusDateAndBranch(status, date, branchId));
    }

    // ❌ GET: No logging
    @GetMapping("/active")
    public ResponseEntity<List<Debt>> getActiveDebts(HttpServletRequest request) {
        Long branchId = getCurrentBranchId(request);
        return ResponseEntity.ok(debtService.getDebtsByStatusesAndBranch(
                List.of(DebtStatus.UNPAID, DebtStatus.PARTIALLY_PAID), branchId));
    }

    // ❌ GET: No logging
    @GetMapping("/all")
    public ResponseEntity<List<Debt>> getAllDebts(HttpServletRequest request) {
        Long branchId = getCurrentBranchId(request);
        return ResponseEntity.ok(debtService.getAllDebtsByBranch(branchId));
    }

    // ✅ PUT: Pay Debt
    @PutMapping("/{id}/pay")
    public ResponseEntity<?> payDebt(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam String mode,
            HttpServletRequest request
    ) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long branchId = getCurrentBranchId(request);
        Optional<Debt> updated = debtService.payDebt(id, amount, mode, branchId);

        if (updated.isPresent()) {
            Debt paidDebt = updated.get();
            log(userDetails.getUsername(), "PAY_DEBT", "Paid " + amount + " (" + mode + ") to " + paidDebt.getDebtorName(), String.valueOf(id));
            return ResponseEntity.ok(paidDebt);
        }

        return ResponseEntity.notFound().build();
    }

    // ❌ GET: No logging
    @GetMapping("/totals")
    public ResponseEntity<Map<String, BigDecimal>> getDebtTotals(HttpServletRequest request) {
        Long branchId = getCurrentBranchId(request);
        return ResponseEntity.ok(debtService.calculateTotalsByBranch(branchId));
    }

    // ❌ GET: No logging
    @GetMapping("/attendant-paid")
    public ResponseEntity<List<Debt>> getDebtsPaidByAttendantOnDate(
            @RequestParam Long attendantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request
    ) {
        Long branchId = getCurrentBranchId(request);
        return ResponseEntity.ok(debtService.getDebtsPaidByAttendantOnDateAndBranch(attendantId, date, branchId));
    }

    // ✅ DELETE: Delete Debt
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDebt(@PathVariable Long id, HttpServletRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long branchId = getCurrentBranchId(request);
        Optional<Debt> optionalDebt = debtService.getDebtByIdAndBranch(id, branchId);

        if (optionalDebt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Debt debt = optionalDebt.get();
        if (debt.getAmountPaid().compareTo(BigDecimal.ZERO) > 0 || debt.getStatus() == DebtStatus.PAID) {
            return ResponseEntity.badRequest().body("Cannot delete a paid or partially paid debt.");
        }

        debtService.deleteDebtById(id);
        log(userDetails.getUsername(), "DELETE_DEBT", "Deleted debt for " + debt.getDebtorName(), String.valueOf(id));
        return ResponseEntity.ok().body("Debt deleted successfully.");
    }
}
