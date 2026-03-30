package com.sadop.energymanagement.controller;

import com.sadop.energymanagement.model.Branch;
import com.sadop.energymanagement.model.FuelRestock;
import com.sadop.energymanagement.model.User;
import com.sadop.energymanagement.repository.BranchRepository;
import com.sadop.energymanagement.security.CustomUserDetails;
import com.sadop.energymanagement.service.FuelRestockService;
import com.sadop.energymanagement.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fuel-restock")
@RequiredArgsConstructor
public class FuelRestockController {

    private final FuelRestockService fuelRestockService;
    private final BranchRepository branchRepository;
    private final AuditService auditService;

    @PostMapping("/save")
    public ResponseEntity<FuelRestock> saveFuelRestock(@RequestBody FuelRestock fuelRestock,
                                                       Authentication authentication,
                                                       @RequestHeader(value = "X-Branch-Id", required = false) Long headerBranchId) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        Long branchId;

        if (user.getRole().name().equals("ROLE_CEO")) {
            if (headerBranchId == null) {
                return ResponseEntity.badRequest().build();
            }
            branchId = headerBranchId;
        } else {
            if (user.getBranch() == null) {
                return ResponseEntity.status(403).build();
            }
            branchId = user.getBranch().getId();
        }

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        fuelRestock.setBranch(branch);
        FuelRestock savedRestock = fuelRestockService.restockFuel(fuelRestock);

        auditService.logActivity(
                user.getEmail(),
                "SAVE_FUEL_RESTOCK",
                "Saved fuel restock of " + savedRestock.getQuantity() + "L of " + savedRestock.getFuelType() + " for branch " + branchId,
                "FuelRestock",
                String.valueOf(savedRestock.getId())
        );

        return ResponseEntity.ok(savedRestock);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<FuelRestock>> getRecentRestocks(@RequestParam("branchId") Long branchId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (!user.getRole().name().equals("ROLE_CEO")) {
            if (user.getBranch() == null || !user.getBranch().getId().equals(branchId)) {
                throw new RuntimeException("Unauthorized access to this branch data");
            }
        }

        List<FuelRestock> result = fuelRestockService.getRecentFuelRestocksByBranch(branchId);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<FuelRestock>> getAllRestocks(@RequestParam("branchId") Long branchId, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (!user.getRole().name().equals("ROLE_CEO")) {
            if (user.getBranch() == null || !user.getBranch().getId().equals(branchId)) {
                throw new RuntimeException("Unauthorized access to this branch data");
            }
        }

        List<FuelRestock> result = fuelRestockService.getAllRestocksByBranch(branchId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<FuelRestock>> getRestocksByFuelType(@RequestParam String fuelType,
                                                                   @RequestParam("branchId") Long branchId,
                                                                   Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if (!user.getRole().name().equals("ROLE_CEO")) {
            if (user.getBranch() == null || !user.getBranch().getId().equals(branchId)) {
                throw new RuntimeException("Unauthorized access to this branch data");
            }
        }

        List<FuelRestock> result = fuelRestockService.getRestocksByFuelTypeAndBranch(fuelType, branchId);
        return ResponseEntity.ok(result);
    }
}
