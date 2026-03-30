package com.sadop.energymanagement.controller;

import com.sadop.energymanagement.model.Branch;
import com.sadop.energymanagement.model.Expense;
import com.sadop.energymanagement.model.ExpenseStatus;
import com.sadop.energymanagement.model.User;
import com.sadop.energymanagement.repository.UserRepository;
import com.sadop.energymanagement.security.CustomUserDetails;
import com.sadop.energymanagement.service.AuditService;
import com.sadop.energymanagement.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Autowired
    public ExpenseController(ExpenseService expenseService, UserRepository userRepository, AuditService auditService) {
        this.expenseService = expenseService;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((CustomUserDetails) authentication.getPrincipal()).getUser();
    }

    private boolean isAuthorized(User user, Long branchId) {
        return (user.getBranch() != null && user.getBranch().getId().equals(branchId))
                || user.getRole().name().equals("ROLE_CEO");
    }

    @PostMapping("/save")
    public ResponseEntity<Expense> createExpense(@RequestBody Expense expense) {
        User user = getLoggedInUser();
        if (user.getBranch() == null && expense.getBranch() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        if (!user.getRole().name().equals("ROLE_CEO")) {
            expense.setBranch(user.getBranch());
        }

        if (expense.getType() == null || expense.getType().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (expense.getStatus() == null) {
            expense.setStatus(ExpenseStatus.DRAFT);
        }

        Expense savedExpense = expenseService.saveExpense(expense, user);

        auditService.logActivity(
                user.getUsername(),
                "CREATE_EXPENSE",
                "Created expense of type: " + savedExpense.getType() + ", amount: " + savedExpense.getAmount(),
                "Expense",
                String.valueOf(savedExpense.getId())
        );

        return new ResponseEntity<>(savedExpense, HttpStatus.OK);
    }

    @PostMapping("/submit")
    public ResponseEntity<List<Expense>> submitExpenses(@RequestBody List<Expense> expenses) {
        User user = getLoggedInUser();
        for (Expense expense : expenses) {
            expense.setStatus(ExpenseStatus.SUBMITTED);

            if (user.getBranch() == null && expense.getBranch() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (!user.getRole().name().equals("ROLE_CEO")) {
                expense.setBranch(user.getBranch());
            }

            Expense saved = expenseService.saveExpense(expense, user);

            auditService.logActivity(
                    user.getUsername(),
                    "SUBMIT_EXPENSE",
                    "Submitted expense of type: " + saved.getType() + ", amount: " + saved.getAmount(),
                    "Expense",
                    String.valueOf(saved.getId())
            );
        }

        return new ResponseEntity<>(expenses, HttpStatus.OK);
    }

    @PutMapping("/submit/{expenseId}")
    public ResponseEntity<Expense> submitExpense(@PathVariable Long expenseId) {
        Optional<Expense> updated = expenseService.submitExpense(expenseId);

        updated.ifPresent(exp -> {
            User user = getLoggedInUser();
            auditService.logActivity(
                    user.getUsername(),
                    "SUBMIT_EXPENSE",
                    "Submitted single expense of type: " + exp.getType() + ", amount: " + exp.getAmount(),
                    "Expense",
                    String.valueOf(exp.getId())
            );
        });

        return updated.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/delete/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long expenseId) {
        User user = getLoggedInUser();

        if (user.getBranch() == null && !user.getRole().name().equals("ROLE_CEO")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        boolean deleted = expenseService.deleteExpenseById(expenseId);

        if (deleted) {
            auditService.logActivity(
                    user.getUsername(),
                    "DELETE_EXPENSE",
                    "Deleted expense with ID: " + expenseId,
                    "Expense",
                    String.valueOf(expenseId)
            );
        }

        return deleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // ✅ GET endpoints — NO AUDIT LOGGING HERE

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Expense>> getExpensesByDate(@PathVariable String date, @RequestParam Long branchId) {
        User user = getLoggedInUser();
        if (!isAuthorized(user, branchId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LocalDate localDate = LocalDate.parse(date);
        return ResponseEntity.ok(expenseService.getExpensesByDateAndBranch(localDate, branchId));
    }

    @GetMapping("/unsubmitted")
    public ResponseEntity<List<Expense>> getUnsubmittedExpenses(@RequestParam Long branchId) {
        User user = getLoggedInUser();
        if (!isAuthorized(user, branchId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(expenseService.getUnsubmittedExpensesByBranch(branchId));
    }

    @GetMapping("/submitted")
    public ResponseEntity<List<Expense>> getSubmittedExpenses(@RequestParam Long branchId) {
        User user = getLoggedInUser();
        if (!isAuthorized(user, branchId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(expenseService.getSubmittedExpensesByBranch(branchId));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long expenseId) {
        Optional<Expense> expense = expenseService.getExpenseById(expenseId);
        return expense.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses(@RequestParam Long branchId) {
        User user = getLoggedInUser();
        if (!isAuthorized(user, branchId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(expenseService.getAllExpensesByBranch(branchId));
    }
}
