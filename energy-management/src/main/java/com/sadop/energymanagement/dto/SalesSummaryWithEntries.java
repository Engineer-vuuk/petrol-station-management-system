package com.sadop.energymanagement.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesSummaryWithEntries {
    private List<SalesEntryResponse> entries;

    private BigDecimal totalCash;
    private BigDecimal totalMpesa;
    private BigDecimal equitel;
    private BigDecimal familyBank;
    private BigDecimal visaCard;
    private BigDecimal coins;
    private BigDecimal creditSales;
    private BigDecimal totalDebts;
    private BigDecimal discount;
    private BigDecimal paidDebts;
    private BigDecimal totalExpenses;
    private BigDecimal expectedCash;
    private BigDecimal fuelConsumed;
    private BigDecimal shortOrLoss;
    private BigDecimal totalProfit;
}
