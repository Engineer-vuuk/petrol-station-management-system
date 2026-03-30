package com.sadop.energymanagement.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesEntryResponse {

    private Long id;
    private String entryNumber;
    private String pumpName;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal totalCash;
    private BigDecimal totalMpesa;
    private BigDecimal totalDebts;
    private BigDecimal pricePerLitre;
    private BigDecimal expectedCash;
    private BigDecimal shortOrLoss;
    private BigDecimal fuelConsumed;

    private BigDecimal buyingPrice;  // ✅ NEW
    private BigDecimal profit;       // ✅ NEW

    private BigDecimal discount;
    private BigDecimal paidDebts;
    private BigDecimal totalExpenses;
    private BigDecimal totalPumpSales;
    private BigDecimal equitel;
    private BigDecimal familyBank;
    private BigDecimal visaCard;
    private BigDecimal otherMobileMoney;
    private BigDecimal creditSales;

    private String remarks;
    private String attendantId;
    private String status;
    private LocalDate salesDate;
    private LocalDateTime submittedAt;

    private String managerName;
    private String branchName;
}
