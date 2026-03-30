package com.sadop.energymanagement.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesEntryRequest {

    private int entryNumber;
    private String pumpName;
    private double openingBalance;
    private double closingBalance;
    private double totalCash;
    private double totalMpesa;
    private double totalDebts;
    private double pricePerLitre;
    private double expectedCash;
    private double shortOrLoss;
    private double fuelConsumed;

    // ✅ New field
    private double buyingPrice;

    // ✅ New calculated field (optional on input, useful for completeness)
    private double profit;

    private double discount;
    private double totalPumpSales;
    private String remarks;
    private LocalDate salesDate;
    private String attendantId;

    private double paidDebts;
    private double totalExpenses;
    private double equitel;
    private double familyBank;
    private double visaCard;
    private double otherMobileMoney;
    private double creditSales;
}
