package com.sadop.energymanagement.controller;

import com.sadop.energymanagement.model.InitialPumpBalance;
import com.sadop.energymanagement.service.InitialPumpBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/initial-pump")
public class InitialPumpBalanceController {

    @Autowired
    private InitialPumpBalanceService service;

    // Only allow users with ROLE_MANAGER or ROLE_ADMIN to create balances
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @PostMapping("/setup")
    public ResponseEntity<?> createInitialBalance(@RequestBody InitialPumpBalance balance) {
        try {
            // Try to save the balance
            return ResponseEntity.ok(service.save(balance));
        } catch (RuntimeException e) {
            // Return error message if an exception occurs
            return ResponseEntity.badRequest().body("❌ Error: " + e.getMessage());
        }
    }

    // Allow users with ROLE_MANAGER or ROLE_ADMIN to get balances
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
    @GetMapping("/get/{pumpName}")
    public ResponseEntity<Double> getInitialBalance(@PathVariable String pumpName) {
        return service.getOpeningBalance(pumpName)
                .map(ResponseEntity::ok) // Return balance if found
                .orElse(ResponseEntity.notFound().build()); // Return 404 if not found
    }
}
