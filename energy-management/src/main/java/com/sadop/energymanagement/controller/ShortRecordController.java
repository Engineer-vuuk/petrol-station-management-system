package com.sadop.energymanagement.controller;

import com.sadop.energymanagement.dto.ShortRequest;
import com.sadop.energymanagement.dto.ShortSummary;
import com.sadop.energymanagement.model.User;
import com.sadop.energymanagement.repository.UserRepository;
import com.sadop.energymanagement.security.CustomUserDetails;
import com.sadop.energymanagement.service.AuditService;
import com.sadop.energymanagement.service.ShortRecordService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shorts")
@RequiredArgsConstructor
@CrossOrigin
public class ShortRecordController {

    private final ShortRecordService shortService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    private Long getCurrentBranchId(HttpServletRequest request, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        if ("ROLE_CEO".equals(user.getRole().name())) {
            String branchIdHeader = request.getHeader("X-Branch-Id");
            if (branchIdHeader != null) {
                return Long.parseLong(branchIdHeader);
            } else {
                throw new RuntimeException("Missing X-Branch-Id header for CEO user");
            }
        } else {
            if (user.getBranch() == null) {
                throw new RuntimeException("User has no assigned branch");
            }
            return user.getBranch().getId();
        }
    }

    @PostMapping("/repay")
    public ResponseEntity<String> repayShort(@RequestBody ShortRequest request,
                                             HttpServletRequest httpRequest,
                                             Authentication authentication) {
        Long branchId = getCurrentBranchId(httpRequest, authentication);
        shortService.repayShort(
                request.getAttendantId(),
                request.getAmount(),
                branchId,
                LocalDate.now()
        );

        logAudit(authentication, httpRequest, "REPAY_SHORT",
                "Repayment of short amount " + request.getAmount() + " for attendant " + request.getAttendantId(),
                "ShortRecord",
                String.valueOf(request.getAttendantId()));

        return ResponseEntity.ok("Short repayment recorded successfully for branch " + branchId);
    }

    @PostMapping("/submit")
    public ResponseEntity<String> submitShorts(HttpServletRequest httpRequest,
                                               Authentication authentication) {
        Long branchId = getCurrentBranchId(httpRequest, authentication);
        shortService.submitAllShorts(branchId);

        logAudit(authentication, httpRequest, "SUBMIT_SHORTS",
                "Submitted all shorts for branch " + branchId,
                "ShortRecord",
                "branch-" + branchId);

        return ResponseEntity.ok("All shorts submitted and reset for branch " + branchId);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ShortSummary>> getAllUnsubmittedShorts(HttpServletRequest httpRequest,
                                                                      Authentication authentication) {
        Long branchId = getCurrentBranchId(httpRequest, authentication);

        // No audit logging here (GET read-only request)

        return ResponseEntity.ok(shortService.getAllUnsubmittedShorts(branchId));
    }

    private void logAudit(Authentication authentication, HttpServletRequest request,
                          String action, String details, String entity, String entityId) {
        String username = ((CustomUserDetails) authentication.getPrincipal()).getUsername();
        auditService.logActivity(
                username,
                action,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                details,
                entity,
                entityId
        );
    }
}
