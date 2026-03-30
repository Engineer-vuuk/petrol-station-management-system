package com.sadop.energymanagement.service;

import com.sadop.energymanagement.model.Expense;
import com.sadop.energymanagement.model.ExpenseStatus;
import com.sadop.energymanagement.model.User;
import com.sadop.energymanagement.repository.ExpenseRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public Expense saveExpense(Expense expense, User user) {
        if (expense.getDate() == null) {
            expense.setDate(LocalDate.now());
        }

        if (expense.getStatus() == null) {
            expense.setStatus(ExpenseStatus.DRAFT);
        }

        if (expense.getBranch() == null && user.getBranch() != null) {
            expense.setBranch(user.getBranch());
        }

        return expenseRepository.save(expense);
    }

    public List<Expense> getExpensesByDate(LocalDate date) {
        return expenseRepository.findByDate(date);
    }

    public List<Expense> getExpensesByDateAndBranch(LocalDate date, Long branchId) {
        return expenseRepository.findByDateAndBranchId(date, branchId);
    }

    public List<Expense> getUnsubmittedExpenses() {
        return expenseRepository.findByStatus(ExpenseStatus.DRAFT);
    }

    public List<Expense> getUnsubmittedExpensesByBranch(Long branchId) {
        return expenseRepository.findByStatusAndBranchId(ExpenseStatus.DRAFT, branchId);
    }

    public List<Expense> getSubmittedExpenses() {
        return expenseRepository.findByStatus(ExpenseStatus.SUBMITTED);
    }

    public List<Expense> getSubmittedExpensesByBranch(Long branchId) {
        return expenseRepository.findByStatusAndBranchId(ExpenseStatus.SUBMITTED, branchId);
    }

    public Optional<Expense> getExpenseById(Long expenseId) {
        return expenseRepository.findById(expenseId);
    }

    public Optional<Expense> submitExpense(Long expenseId) {
        Optional<Expense> expense = expenseRepository.findById(expenseId);
        if (expense.isPresent()) {
            Expense updatedExpense = expense.get();
            updatedExpense.setStatus(ExpenseStatus.SUBMITTED);
            expenseRepository.save(updatedExpense);
            return Optional.of(updatedExpense);
        }
        return Optional.empty();
    }

    public List<Expense> submitExpenses(List<Expense> expenses) {
        for (Expense expense : expenses) {
            expense.setStatus(ExpenseStatus.SUBMITTED);
            expenseRepository.save(expense);
        }
        return expenses;
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public List<Expense> getAllExpensesByBranch(Long branchId) {
        return expenseRepository.findByBranchId(branchId);
    }

    public boolean deleteExpenseById(Long id) {
        Optional<Expense> optional = expenseRepository.findById(id);
        if (optional.isPresent()) {
            expenseRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
