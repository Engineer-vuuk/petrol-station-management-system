package com.sadop.energymanagement.service;

import com.sadop.energymanagement.dto.ShortSummary;
import com.sadop.energymanagement.model.ShortRecord;
import com.sadop.energymanagement.model.User;
import com.sadop.energymanagement.repository.ShortRecordRepository;
import com.sadop.energymanagement.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ShortRecordService {

    @Autowired
    private ShortRecordRepository shortRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private HttpServletRequest request;

    // ✅ Helper to save short or repayment
    private void saveShort(Long userId, BigDecimal amount, Long branchId, LocalDate date) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getBranch() == null || user.getBranch().getId() == null) {
            throw new RuntimeException("User has no branch assigned!");
        }

        if (!user.getBranch().getId().equals(branchId)) {
            throw new RuntimeException("User does not belong to this branch.");
        }

        ShortRecord record = new ShortRecord();
        record.setAttendant(user);
        record.setAmount(amount);
        record.setDate(date); // ✅ FIXED (uses sales date)
        record.setSubmitted(false);
        record.setLastUpdated(LocalDateTime.now());
        record.setLastAction(amount.compareTo(BigDecimal.ZERO) >= 0
                ? ShortRecord.Action.ADD
                : ShortRecord.Action.REPAY);
        record.setBranch(user.getBranch());

        shortRepo.save(record);
    }

    public void addShort(Long userId, BigDecimal amount, Long branchId, LocalDate date) {
        saveShort(userId, amount, branchId, date);
    }
    public void repayShort(Long userId, BigDecimal amount, Long branchId, LocalDate date) {
        saveShort(userId, amount.negate(), branchId, date);
    }

    public void submitAllShorts(Long branchId) {
        List<ShortRecord> shorts = shortRepo.findBySubmittedFalseAndAttendant_Branch_Id(branchId);
        for (ShortRecord s : shorts) {
            s.setSubmitted(true);
        }
        shortRepo.saveAll(shorts);
    }

    public List<ShortSummary> getAllUnsubmittedShorts(Long branchId) {
        List<ShortRecord> records = shortRepo.findBySubmittedFalseAndAttendant_Branch_Id(branchId);

        Map<Long, ShortSummary> summaryMap = new HashMap<>();

        for (ShortRecord record : records) {
            Long id = record.getAttendant().getId();
            ShortSummary summary = summaryMap.getOrDefault(id, new ShortSummary());
            summary.setAttendantId(id);
            summary.setAttendantName(record.getAttendant().getFullName());

            BigDecimal currentTotal = summary.getTotalShort() == null ? BigDecimal.ZERO : summary.getTotalShort();
            summary.setTotalShort(currentTotal.add(record.getAmount()));

            if (summary.getLastUpdated() == null || record.getLastUpdated().isAfter(summary.getLastUpdated())) {
                summary.setLastUpdated(record.getLastUpdated());
                summary.setLastAction(record.getAmount().compareTo(BigDecimal.ZERO) >= 0 ? "ADD" : "REPAY");
            }

            summaryMap.put(id, summary);
        }

        return new ArrayList<>(summaryMap.values());
    }

    private String getUsername() {
        return request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "SYSTEM";
    }
}
