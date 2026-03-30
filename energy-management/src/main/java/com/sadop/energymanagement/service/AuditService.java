package com.sadop.energymanagement.service;

import com.sadop.energymanagement.model.AuditLog;
import com.sadop.energymanagement.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    public AuditService(AuditLogRepository auditLogRepository, HttpServletRequest request) {
        this.auditLogRepository = auditLogRepository;
        this.request = request;
    }

    /**
     * Standard full logging method (used internally).
     */
    public void logActivity(String username, String action, String method, String path,
                            String ipAddress, String details, String entity, String entityId) {

        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action);
        log.setMethod(method);
        log.setPath(path);
        log.setIpAddress(ipAddress);
        log.setDetails(details);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setTimestamp(LocalDateTime.now());

        auditLogRepository.save(log);
    }

    /**
     * Context-aware logging (uses current request).
     */
    public void logActivity(String username, String action, String details, String entity, String entityId) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        String ip = request.getRemoteAddr();

        logActivity(username, action, method, path, ip, details, entity, entityId);
    }

    /**
     * Smart logger: Skips GET/OPTIONS and prevents duplicates within 5 seconds.
     */
    public void logActivityIfNeeded(String username, String action, String details, String entity, String entityId) {
        String method = request.getMethod();

        // 🚫 Skip GET and OPTIONS
        if ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method)) {
            return;
        }

        // Check for recent duplicate log
        Optional<AuditLog> recent = auditLogRepository
                .findTopByUsernameAndActionAndEntityAndEntityIdOrderByTimestampDesc(
                        username, action, entity, entityId);

        if (recent.isPresent()) {
            AuditLog lastLog = recent.get();
            LocalDateTime now = LocalDateTime.now();

            if (lastLog.getTimestamp().isAfter(now.minusSeconds(5))) {
                return; // ✅ Duplicate within 5 seconds → skip
            }
        }

        // Otherwise, log as usual
        logActivity(username, action, details, entity, entityId);
    }
}
