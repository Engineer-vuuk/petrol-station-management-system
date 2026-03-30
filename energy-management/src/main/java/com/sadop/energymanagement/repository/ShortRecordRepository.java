package com.sadop.energymanagement.repository;

import com.sadop.energymanagement.model.ShortRecord;
import com.sadop.energymanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShortRecordRepository extends JpaRepository<ShortRecord, Long> {

    // Find shorts for a specific attendant (if needed)
    List<ShortRecord> findByAttendantAndSubmittedFalse(User attendant);

    // Find all unsubmitted shorts globally (use carefully)
    List<ShortRecord> findBySubmittedFalse();

    // ✅ NEW: Find all unsubmitted shorts for a specific branch only (strict branch isolation)
    List<ShortRecord> findBySubmittedFalseAndAttendant_Branch_Id(Long branchId);
}
