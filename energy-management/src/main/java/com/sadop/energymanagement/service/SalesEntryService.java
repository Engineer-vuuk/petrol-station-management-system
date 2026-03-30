package com.sadop.energymanagement.service;

import com.sadop.energymanagement.dto.SalesEntryResponse;
import com.sadop.energymanagement.dto.SalesSummaryWithEntries;
import com.sadop.energymanagement.model.SalesEntry;
import com.sadop.energymanagement.repository.SalesEntryRepository;
import com.sadop.energymanagement.repository.UserRepository;
import com.sadop.energymanagement.repository.BranchRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import java.security.SecureRandom;
import java.util.stream.Collectors;

@Service
public class SalesEntryService {

    @Autowired
    private SalesEntryRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ShortRecordService shortRecordService;

    @Autowired
    private TankService tankService;

    private static final SecureRandom random = new SecureRandom();
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";

    @Transactional
    public SalesEntry saveDraft(SalesEntry entry, Long branchId) {
        entry.setStatus("draft");

        if (entry.getOpeningBalance() == null || entry.getClosingBalance() == null) {
            throw new IllegalArgumentException("Opening and closing balance must not be null.");
        }

        BigDecimal fuelSold = entry.getClosingBalance().subtract(entry.getOpeningBalance());
        if (fuelSold.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Closing balance cannot be less than opening balance.");
        }

        double availableFuel = tankService.getFuelLevelByPumpNameAndBranch(entry.getPumpName(), branchId);
        if (fuelSold.doubleValue() > availableFuel) {
            throw new IllegalArgumentException("Not enough fuel in tank for pump " + entry.getPumpName()
                    + " in branch ID " + branchId + ". Available: " + availableFuel + "L, Requested: " + fuelSold + "L.");
        }

        if (entry.getSubmittedAt() == null) {
            entry.setSubmittedAt(LocalDateTime.now());
        }

        if (entry.getEntryNumber() == null || entry.getEntryNumber().isEmpty()) {
            entry.setEntryNumber(generateUniqueEntryNumber());
        }

        return repository.saveAndFlush(entry);
    }

    private String generateUniqueEntryNumber() {
        final int maxAttempts = 10;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            String candidate = generateEntryNumber();
            if (!repository.existsByEntryNumber(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Failed to generate unique entry number.");
    }

    private String generateEntryNumber() {
        List<Character> chars = new ArrayList<>();
        for (int i = 0; i < 2; i++) chars.add(LETTERS.charAt(random.nextInt(LETTERS.length())));
        for (int i = 0; i < 3; i++) chars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        Collections.shuffle(chars, random);
        return chars.stream().map(String::valueOf).collect(Collectors.joining());
    }

    public Optional<SalesEntry> getDraftById(Long id) {
        return repository.findById(id);
    }

    public List<SalesEntry> getDraftsByBranch(Long branchId) {
        return repository.findByStatusAndBranchId("draft", branchId);
    }

    public List<SalesEntry> submitDraftsByBranch(Long branchId) {

        List<SalesEntry> drafts = repository.findByStatusAndBranchId("draft", branchId);

        for (SalesEntry entry : drafts) {

            // ✅ Create short automatically if exists
            if (entry.getShortOrLoss() != null &&
                    entry.getShortOrLoss().compareTo(BigDecimal.ZERO) > 0) {

                Long attendantId = Long.valueOf(entry.getAttendantId());

                shortRecordService.addShort(
                        attendantId,
                        entry.getShortOrLoss(),
                        branchId,
                        entry.getSalesDate()  // ✅ Uses actual sales date
                );
            }

            entry.setStatus("submitted");
            entry.setSubmittedAt(LocalDateTime.now());
        }

        return repository.saveAll(drafts);
    }

    public List<SalesEntry> getSubmittedSalesByDateAndBranch(LocalDate date, Long branchId) {
        return repository.findBySalesDateAndStatusAndBranchId(date, "submitted", branchId);
    }

    public double getLastClosingBalanceForPumpAndBranch(String pumpName, Long branchId) {
        Pageable first = PageRequest.of(0, 1);
        List<SalesEntry> entries = repository.findMostRecentByPumpNameAndBranchId(pumpName, branchId, first);
        return (!entries.isEmpty() && entries.get(0).getClosingBalance() != null)
                ? entries.get(0).getClosingBalance().doubleValue() : 0.0;
    }

    public SalesSummaryWithEntries getTodaySummary(Long branchId) {
        LocalDate today = LocalDate.now();
        return getSubmittedSalesBetweenDates(today, today, branchId);
    }

    public void deleteDraftById(Long id) {
        SalesEntry entry = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found with ID: " + id));

        if (!"draft".equalsIgnoreCase(entry.getStatus())) {
            throw new IllegalStateException("Only draft entries can be deleted.");
        }

        repository.deleteById(id);
    }

    public SalesSummary getDraftSummaryByBranch(Long branchId) {
        return new SalesSummary(); // Business logic omitted
    }

    public SalesEntryResponse mapToResponse(SalesEntry e) {
        return SalesEntryResponse.builder()
                .id(e.getId())
                .entryNumber(e.getEntryNumber())
                .pumpName(e.getPumpName())
                .openingBalance(e.getOpeningBalance())
                .closingBalance(e.getClosingBalance())
                .totalCash(e.getTotalCash())
                .totalMpesa(e.getTotalMpesa())
                .totalDebts(e.getTotalDebts())
                .pricePerLitre(e.getPricePerLitre())
                .expectedCash(e.getExpectedCash())
                .shortOrLoss(e.getShortOrLoss())
                .fuelConsumed(e.getFuelConsumed())

                .buyingPrice(e.getBuyingPrice()) // ✅ NEW
                .profit(e.getProfit())           // ✅ NEW

                .discount(e.getDiscount())
                .paidDebts(e.getPaidDebts())
                .totalExpenses(e.getTotalExpenses())
                .totalPumpSales(e.getTotalPumpSales())
                .equitel(e.getEquitel())
                .familyBank(e.getFamilyBank())
                .visaCard(e.getVisaCard())
                .otherMobileMoney(e.getOtherMobileMoney())
                .creditSales(e.getCreditSales())
                .remarks(e.getRemarks())
                .attendantId(e.getAttendantId())
                .status(e.getStatus())
                .salesDate(e.getSalesDate())
                .submittedAt(e.getSubmittedAt())
                .managerName(e.getManager() != null ? e.getManager().getFullName() : null)
                .branchName(e.getBranch() != null ? e.getBranch().getBranchName() : null)
                .build();
    }


    public static class SalesSummary {
    }


    public SalesSummaryWithEntries getSubmittedSalesBetweenDates(LocalDate startDate, LocalDate endDate, Long branchId) {
        List<SalesEntry> entries = repository.findSubmittedBetweenDates(startDate, endDate, branchId);

        List<SalesEntryResponse> responses = entries.stream()
                .map(this::mapToResponse)
                .toList();

        SalesSummaryWithEntries summary = SalesSummaryWithEntries.builder()
                .entries(responses)
                .totalCash(BigDecimal.ZERO)
                .totalMpesa(BigDecimal.ZERO)
                .equitel(BigDecimal.ZERO)
                .familyBank(BigDecimal.ZERO)
                .visaCard(BigDecimal.ZERO)
                .coins(BigDecimal.ZERO)
                .creditSales(BigDecimal.ZERO)
                .totalDebts(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .paidDebts(BigDecimal.ZERO)
                .totalExpenses(BigDecimal.ZERO)
                .expectedCash(BigDecimal.ZERO)
                .fuelConsumed(BigDecimal.ZERO)
                .shortOrLoss(BigDecimal.ZERO)
                .totalProfit(BigDecimal.ZERO)
                .build();

        for (SalesEntryResponse r : responses) {
            summary.setTotalCash(summary.getTotalCash().add(safe(r.getTotalCash())));
            summary.setTotalMpesa(summary.getTotalMpesa().add(safe(r.getTotalMpesa())));
            summary.setEquitel(summary.getEquitel().add(safe(r.getEquitel())));
            summary.setFamilyBank(summary.getFamilyBank().add(safe(r.getFamilyBank())));
            summary.setVisaCard(summary.getVisaCard().add(safe(r.getVisaCard())));
            summary.setCoins(summary.getCoins().add(safe(r.getOtherMobileMoney())));
            summary.setCreditSales(summary.getCreditSales().add(safe(r.getCreditSales())));
            summary.setTotalDebts(summary.getTotalDebts().add(safe(r.getTotalDebts())));
            summary.setDiscount(summary.getDiscount().add(safe(r.getDiscount())));
            summary.setPaidDebts(summary.getPaidDebts().add(safe(r.getPaidDebts())));
            summary.setTotalExpenses(summary.getTotalExpenses().add(safe(r.getTotalExpenses())));
            summary.setExpectedCash(summary.getExpectedCash().add(safe(r.getExpectedCash())));
            summary.setFuelConsumed(summary.getFuelConsumed().add(safe(r.getFuelConsumed())));
            summary.setShortOrLoss(summary.getShortOrLoss().add(safe(r.getShortOrLoss())));
            summary.setTotalProfit(summary.getTotalProfit().add(safe(r.getProfit())));
        }

        return summary;
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

}
