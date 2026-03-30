package com.sadop.energymanagement.controller;


import com.sadop.energymanagement.dto.SalesSummaryWithEntries;
import com.sadop.energymanagement.model.Branch;
import com.sadop.energymanagement.dto.SalesEntryRequest;
import com.sadop.energymanagement.dto.SalesEntryResponse;
import com.sadop.energymanagement.model.Role;
import com.sadop.energymanagement.model.SalesEntry;
import com.sadop.energymanagement.model.User;
import com.sadop.energymanagement.repository.UserRepository;
import com.sadop.energymanagement.service.AuditService;
import com.sadop.energymanagement.service.DebtService;
import com.sadop.energymanagement.service.SalesEntryService;
import com.sadop.energymanagement.service.TankService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sales-entries")
public class SalesEntryController {

    @Autowired
    private SalesEntryService service;

    @Autowired
    private DebtService debtService;

    @Autowired
    private TankService tankService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    private boolean isCeo(User user) {
        return user.getRole().name().equals("ROLE_CEO");
    }

    private Long getCurrentBranchId(HttpServletRequest request, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (isCeo(user)) {
            String branchIdHeader = request.getHeader("X-Branch-Id");
            if (branchIdHeader != null) {
                return Long.parseLong(branchIdHeader);
            } else {
                throw new RuntimeException("X-Branch-Id header is missing for CEO user");
            }
        } else {
            return user.getBranch().getId();
        }
    }

    @PostMapping("/save")
    public SalesEntry saveDraft(@RequestBody SalesEntryRequest request, HttpServletRequest httpRequest, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long branchId = getCurrentBranchId(httpRequest, principal);

        Branch branch = new Branch();
        branch.setId(branchId);

        SalesEntry entry = SalesEntry.builder()
                .salesDate(request.getSalesDate())
                .pumpName(request.getPumpName())
                .attendantId(request.getAttendantId())
                .openingBalance(BigDecimal.valueOf(request.getOpeningBalance()))
                .closingBalance(BigDecimal.valueOf(request.getClosingBalance()))
                .pricePerLitre(BigDecimal.valueOf(request.getPricePerLitre()))
                .buyingPrice(BigDecimal.valueOf(request.getBuyingPrice()))  // ✅ Add this line
                .totalCash(BigDecimal.valueOf(request.getTotalCash()))
                .totalMpesa(BigDecimal.valueOf(request.getTotalMpesa()))
                .equitel(BigDecimal.valueOf(request.getEquitel()))
                .familyBank(BigDecimal.valueOf(request.getFamilyBank()))
                .visaCard(BigDecimal.valueOf(request.getVisaCard()))
                .otherMobileMoney(BigDecimal.valueOf(request.getOtherMobileMoney()))
                .creditSales(BigDecimal.valueOf(request.getCreditSales()))
                .totalDebts(BigDecimal.valueOf(request.getTotalDebts()))
                .discount(BigDecimal.valueOf(request.getDiscount()))
                .paidDebts(BigDecimal.valueOf(request.getPaidDebts()))
                .totalExpenses(BigDecimal.valueOf(request.getTotalExpenses()))
                .fuelConsumed(BigDecimal.valueOf(request.getFuelConsumed()))
                .expectedCash(BigDecimal.valueOf(request.getExpectedCash()))
                .shortOrLoss(BigDecimal.valueOf(request.getShortOrLoss()))
                .totalPumpSales(BigDecimal.valueOf(request.getTotalPumpSales()))
                .remarks(request.getRemarks())
                .branch(branch)
                .manager(user)
                .build();

        SalesEntry savedEntry = service.saveDraft(entry, branchId);

        if (entry.getFuelConsumed() != null &&
                entry.getFuelConsumed().compareTo(BigDecimal.ZERO) > 0 &&
                entry.getPumpName() != null && !entry.getPumpName().isEmpty()) {
            tankService.subtractFuelFromTankByPumpAndBranch(entry.getPumpName(), entry.getFuelConsumed(), branchId);
        }

        auditService.logActivity(
                user.getUsername(),
                "SAVE_SALES_DRAFT",
                "Saved sales entry as draft",
                "SalesEntry",
                String.valueOf(savedEntry.getId())
        );

        return savedEntry;
    }

    @GetMapping("/get-drafts")
    public List<SalesEntryResponse> getDrafts(HttpServletRequest request, Principal principal) {
        Long branchId = getCurrentBranchId(request, principal);
        return service.getDraftsByBranch(branchId).stream()
                .map(service::mapToResponse)
                .toList();
    }

    @PostMapping("/submit")
    public List<SalesEntryResponse> submitDrafts(HttpServletRequest request, Principal principal) {
        Long branchId = getCurrentBranchId(request, principal);
        List<SalesEntryResponse> submitted = service.submitDraftsByBranch(branchId).stream()
                .map(service::mapToResponse)
                .toList();

        auditService.logActivity(principal.getName(), "SUBMIT_SALES", "Submitted all sales drafts", "SalesEntry", "BATCH");

        return submitted;
    }

    @GetMapping("/get-draft/{id}")
    public Optional<SalesEntry> getDraftById(@PathVariable Long id, HttpServletRequest request, Principal principal) {
        Long branchId = getCurrentBranchId(request, principal);

        Optional<SalesEntry> entry = service.getDraftById(id);
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (entry.isPresent() && !isCeo(user) && !entry.get().getBranch().getId().equals(branchId)) {
            throw new RuntimeException("Unauthorized access to this branch data");
        }

        return entry;
    }

    @GetMapping("/summary")
    public SalesEntryService.SalesSummary getSummary(HttpServletRequest request, Principal principal) {
        Long branchId = getCurrentBranchId(request, principal);
        return service.getDraftSummaryByBranch(branchId);
    }

    @GetMapping("/get-last-closing-balance/{pumpName}")
    public double getLastClosingBalance(@PathVariable String pumpName, HttpServletRequest request, Principal principal) {
        Long branchId = getCurrentBranchId(request, principal);
        return service.getLastClosingBalanceForPumpAndBranch(pumpName, branchId);
    }

    @GetMapping("/submitted")
    public List<SalesEntryResponse> getSubmittedSalesByDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request,
            Principal principal) {
        Long branchId = getCurrentBranchId(request, principal);
        return service.getSubmittedSalesByDateAndBranch(date, branchId).stream()
                .map(service::mapToResponse)
                .toList();
    }

    @DeleteMapping("/delete/{id}")
    public void deleteDraft(@PathVariable Long id, HttpServletRequest request, Principal principal) {
        Long branchId = getCurrentBranchId(request, principal);

        Optional<SalesEntry> entry = service.getDraftById(id);
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (entry.isPresent() && !isCeo(user) && !entry.get().getBranch().getId().equals(branchId)) {
            throw new RuntimeException("Unauthorized access to this branch data");
        }

        service.deleteDraftById(id);

        auditService.logActivity(user.getUsername(), "DELETE_SALES_DRAFT", "Deleted draft entry ID: " + id, "SalesEntry", String.valueOf(id));
    }
    @GetMapping("/submitted-between")
    public SalesSummaryWithEntries getSubmittedSalesBetweenDates(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request,
            Principal principal) {

        Long branchId = getCurrentBranchId(request, principal);
        return service.getSubmittedSalesBetweenDates(startDate, endDate, branchId);
    }
    @GetMapping("/attendants")
    public List<Map<String, Object>> getAttendantsByBranch(HttpServletRequest request, Principal principal) {
        Long branchId = getCurrentBranchId(request, principal);

        List<User> attendants = userRepository.findByRoleAndBranch_Id(Role.ROLE_ATTENDANT, branchId);

        // Map to a simplified structure for frontend
        return attendants.stream().map(a -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", a.getId());
            map.put("fullName", a.getFullName());
            map.put("email", a.getEmail());
            map.put("phone", a.getPhone());
            map.put("branchId", a.getBranch() != null ? a.getBranch().getId() : null);
            return map;
        }).toList();
    }

    @GetMapping("/today-summary")
    public SalesSummaryWithEntries getTodaySummary(HttpServletRequest request, Principal principal) {
        Long branchId = getCurrentBranchId(request, principal); // already handles CEO vs regular
        return service.getTodaySummary(branchId); // method from SalesEntryService
    }


    @GetMapping("/validate-attendant/{attendantId}")
    public ResponseEntity<Map<String, Boolean>> validateAttendantId(
            @PathVariable Long attendantId,
            HttpServletRequest request,
            Principal principal
    ) {
        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long currentBranchId;
        if ("ROLE_CEO".equals(currentUser.getRole().name())) {
            String branchIdHeader = request.getHeader("X-Branch-Id");
            if (branchIdHeader == null) {
                throw new RuntimeException("Missing X-Branch-Id header for CEO user");
            }
            currentBranchId = Long.parseLong(branchIdHeader);
        } else {
            if (currentUser.getBranch() == null) {
                throw new RuntimeException("User has no branch assigned");
            }
            currentBranchId = currentUser.getBranch().getId();
        }

        Optional<User> optionalAttendant = userRepository.findById(attendantId);

        boolean isValid = optionalAttendant.isPresent()
                && optionalAttendant.get().getRole() == Role.ROLE_ATTENDANT
                && optionalAttendant.get().getBranch() != null
                && optionalAttendant.get().getBranch().getId().equals(currentBranchId);

        return ResponseEntity.ok(Map.of("valid", isValid));
    }

}
