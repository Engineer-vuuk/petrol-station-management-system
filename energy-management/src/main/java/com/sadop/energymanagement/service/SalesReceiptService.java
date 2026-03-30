package com.sadop.energymanagement.service;

import com.sadop.energymanagement.model.SalesEntry;
import com.sadop.energymanagement.model.SalesReceipt;
import com.sadop.energymanagement.repository.SalesEntryRepository;
import com.sadop.energymanagement.repository.SalesReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SalesReceiptService {

    @Autowired
    private SalesEntryRepository salesEntryRepository;

    @Autowired
    private SalesReceiptRepository salesReceiptRepository;

    public SalesReceipt generateAndSaveReceipt(String managerId) {
        List<SalesEntry> submittedEntries = salesEntryRepository.findByStatus("submitted");

        // Initialize BigDecimal variables for totals
        BigDecimal totalPetrol = BigDecimal.ZERO;
        BigDecimal totalDiesel = BigDecimal.ZERO;
        BigDecimal totalCash = BigDecimal.ZERO;
        BigDecimal totalMpesa = BigDecimal.ZERO;
        BigDecimal totalDebts = BigDecimal.ZERO;
        BigDecimal expectedCash = BigDecimal.ZERO;
        BigDecimal totalPumpSales = BigDecimal.ZERO;
        BigDecimal shortOrLoss = BigDecimal.ZERO;

        for (SalesEntry entry : submittedEntries) {
            String pump = entry.getPumpName().toLowerCase();

            // Summing fuel consumed (petrol or diesel)
            if (pump.contains("petrol")) {
                totalPetrol = totalPetrol.add(entry.getFuelConsumed());
            } else if (pump.contains("diesel")) {
                totalDiesel = totalDiesel.add(entry.getFuelConsumed());
            }

            // Summing other values with proper BigDecimal conversion
            totalCash = totalCash.add(entry.getTotalCash());
            totalMpesa = totalMpesa.add(entry.getTotalMpesa());
            totalDebts = totalDebts.add(entry.getTotalDebts());
            expectedCash = expectedCash.add(entry.getExpectedCash());
            totalPumpSales = totalPumpSales.add(entry.getTotalPumpSales());
            shortOrLoss = shortOrLoss.add(entry.getShortOrLoss());
        }

        SalesReceipt receipt = new SalesReceipt();
        receipt.setReceiptDate(LocalDate.now());
        receipt.setManagerId(managerId);
        receipt.setSubmittedEntries(submittedEntries);
        receipt.setTotalPetrol(totalPetrol.doubleValue()); // Convert BigDecimal to double if needed
        receipt.setTotalDiesel(totalDiesel.doubleValue()); // Convert BigDecimal to double if needed
        receipt.setTotalCash(totalCash.doubleValue()); // Convert BigDecimal to double if needed
        receipt.setTotalMpesa(totalMpesa.doubleValue()); // Convert BigDecimal to double if needed
        receipt.setTotalDebts(totalDebts.doubleValue()); // Convert BigDecimal to double
        receipt.setExpectedCash(expectedCash.doubleValue()); // Convert BigDecimal to double if needed
        receipt.setTotalPumpSales(totalPumpSales.doubleValue()); // Convert BigDecimal to double if needed
        receipt.setShortOrLoss(shortOrLoss.doubleValue()); // Convert BigDecimal to double if needed

        return salesReceiptRepository.save(receipt);
    }

    public List<SalesReceipt> getReceiptsByDate(LocalDate date) {
        return salesReceiptRepository.findByReceiptDate(date);
    }
}
