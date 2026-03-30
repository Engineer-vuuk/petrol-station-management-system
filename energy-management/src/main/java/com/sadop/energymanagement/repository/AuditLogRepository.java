package com.sadop.energymanagement.repository;

import com.sadop.energymanagement.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // ✅ Find the most recent similar log by user, action, entity, and entityId
    Optional<AuditLog> findTopByUsernameAndActionAndEntityAndEntityIdOrderByTimestampDesc(
            String username,
            String action,
            String entity,
            String entityId
    );
}
