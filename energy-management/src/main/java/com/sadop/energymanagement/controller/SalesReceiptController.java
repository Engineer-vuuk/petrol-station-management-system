package com.sadop.energymanagement.controller;

import com.sadop.energymanagement.model.SalesReceipt;
import com.sadop.energymanagement.service.SalesReceiptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/receipts")
public class SalesReceiptController {

    @Autowired
    private SalesReceiptService salesReceiptService;

    @PostMapping("/submit")
    public SalesReceipt submitSalesAndGenerateReceipt(@RequestParam String managerId) {
        return salesReceiptService.generateAndSaveReceipt(managerId);
    }

    @GetMapping("/by-date")
    public List<SalesReceipt> getReceiptsByDate(@RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return salesReceiptService.getReceiptsByDate(parsedDate);
    }
}
