package com.sadop.energymanagement.repository;

import com.sadop.energymanagement.model.SalesEntry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesEntryRepository extends JpaRepository<SalesEntry, Long> {

    List<SalesEntry> findByStatus(String status);

    Optional<SalesEntry> findTopByStatusOrderByCreatedAtDesc(String status);

    List<SalesEntry> findBySalesDate(LocalDate salesDate);

    Optional<SalesEntry> findByPumpNameAndSalesDate(String pumpName, LocalDate salesDate);

    List<SalesEntry> findBySalesDateAndStatus(LocalDate salesDate, String status);

    List<SalesEntry> findBySalesDateAndStatusIn(LocalDate salesDate, List<String> statuses);

    List<SalesEntry> findByPumpNameOrderBySalesDateDesc(String pumpName);

    SalesEntry findFirstByPumpNameOrderBySalesDateDesc(String pumpName);

    SalesEntry findTopByPumpNameAndStatusOrderBySalesDateDesc(String pumpName, String status);

    // ✅ UPDATED: Fetch most recent by pumpName ordered only by createdAt
    @Query("SELECT s FROM SalesEntry s WHERE s.pumpName = :pumpName ORDER BY s.createdAt DESC")
    List<SalesEntry> findMostRecentByPumpName(@Param("pumpName") String pumpName, Pageable pageable);

    // ✅ Check if an entry number exists
    boolean existsByEntryNumber(String entryNumber);

    // ✅ Find submitted sales by date and branch
    List<SalesEntry> findBySalesDateAndStatusAndBranchId(LocalDate salesDate, String status, Long branchId);

    // ✅ Find drafts by branch
    List<SalesEntry> findByStatusAndBranchId(String status, Long branchId);

    // ✅ Find most recent entry by pumpName and branchId (by createdAt)
    @Query("SELECT s FROM SalesEntry s WHERE s.pumpName = :pumpName AND s.branch.id = :branchId ORDER BY s.createdAt DESC")
    List<SalesEntry> findMostRecentByPumpNameAndBranchId(@Param("pumpName") String pumpName, @Param("branchId") Long branchId, Pageable pageable);

    // ✅ Find submitted sales between two dates for a branch
    @Query("SELECT s FROM SalesEntry s WHERE s.status = 'submitted' AND s.salesDate BETWEEN :startDate AND :endDate AND s.branch.id = :branchId")
    List<SalesEntry> findSubmittedBetweenDates(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("branchId") Long branchId
    );
}
