package com.sadop.energymanagement.controller;

import com.sadop.energymanagement.model.User;
import com.sadop.energymanagement.security.CustomUserDetails;
import com.sadop.energymanagement.service.TankService;
import com.sadop.energymanagement.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/fuel-tanks")
@RequiredArgsConstructor
public class FuelTankController {

    private final TankService tankService;
    private final AuditService auditService;

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new RuntimeException("Unauthorized access");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getUser();
    }

    private void validateBranchAccess(User user, Long branchId) {
        if (!user.getRole().name().equals("ROLE_CEO")) {
            if (user.getBranch() == null || !user.getBranch().getId().equals(branchId)) {
                throw new RuntimeException("Forbidden: Access to this branch denied");
            }
        }
    }

    @GetMapping("/total")
    public ResponseEntity<?> getTotalFuelLevels(@RequestParam("branchId") Long branchId, Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
            validateBranchAccess(user, branchId);

            BigDecimal petrolTotal = tankService.getTotalFuelByTypeAndBranch("Petrol", branchId);
            BigDecimal dieselTotal = tankService.getTotalFuelByTypeAndBranch("Diesel", branchId);

            // ❌ No audit log for GET
            return ResponseEntity.ok(Map.of("petrol", petrolTotal, "diesel", dieselTotal));
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PostMapping("/restock")
    public ResponseEntity<?> restockFuelTank(@RequestParam String type,
                                             @RequestParam Double amount,
                                             @RequestParam("branchId") Long branchId,
                                             Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
            validateBranchAccess(user, branchId);

            tankService.addFuelToTank(type, BigDecimal.valueOf(amount), branchId);

            auditService.logActivity(
                    user.getUsername(),
                    "RESTOCK_FUEL",
                    "Restocked " + amount + "L of " + type + " for branch ID: " + branchId,
                    "FuelTank",
                    "branch-" + branchId
            );

            return ResponseEntity.ok("Fuel restocked successfully for " + type + " in branch " + branchId);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @PostMapping("/consume")
    public ResponseEntity<?> consumeFuel(@RequestParam String type,
                                         @RequestParam Double amount,
                                         @RequestParam("branchId") Long branchId,
                                         Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
            validateBranchAccess(user, branchId);

            tankService.subtractFuelFromTank(type, BigDecimal.valueOf(amount), branchId);

            auditService.logActivity(
                    user.getUsername(),
                    "CONSUME_FUEL",
                    "Consumed " + amount + "L of " + type + " for branch ID: " + branchId,
                    "FuelTank",
                    "branch-" + branchId
            );

            return ResponseEntity.ok("Fuel consumed successfully for " + type + " in branch " + branchId);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping("/check-fuel")
    public ResponseEntity<?> checkFuelAvailability(@RequestParam String pumpName,
                                                   @RequestParam Double amount,
                                                   @RequestParam("branchId") Long branchId,
                                                   Authentication authentication) {
        try {
            User user = getAuthenticatedUser(authentication);
            validateBranchAccess(user, branchId);

            boolean hasFuel = tankService.getTankByPumpNameAndBranch(pumpName, branchId)
                    .map(tank -> tank.getCurrentLevel().compareTo(BigDecimal.valueOf(amount)) >= 0)
                    .orElse(false);

            // ❌ No audit log for GET
            return ResponseEntity.ok(hasFuel);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}
