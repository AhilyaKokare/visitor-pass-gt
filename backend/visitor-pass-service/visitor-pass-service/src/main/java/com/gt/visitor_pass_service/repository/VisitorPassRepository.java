package com.gt.visitor_pass_service.repository;

import com.gt.visitor_pass_service.model.VisitorPass;
import com.gt.visitor_pass_service.model.enums.PassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VisitorPassRepository extends JpaRepository<VisitorPass, Long> {

    Page<VisitorPass> findByTenantId(Long tenantId, Pageable pageable);

    Page<VisitorPass> findByCreatedById(Long userId, Pageable pageable);

    Optional<VisitorPass> findByTenantIdAndPassCode(Long tenantId, String passCode);

    @Query("SELECT vp FROM VisitorPass vp WHERE vp.status = :status AND vp.visitDateTime < :now")
    List<VisitorPass> findOverdueApprovedPasses(@Param("status") PassStatus status, @Param("now") LocalDateTime now);

    // --- THIS IS THE REPLACEMENT FOR THE OLD SECURITY DASHBOARD QUERY ---
    /**
     * Finds a paginated list of today's visitors for a specific tenant and status.
     * This is the core query for the paginated security dashboard.
     * @param tenantId The ID of the tenant.
     * @param date The current date (today).
     * @param status The specific PassStatus to filter by (e.g., APPROVED, CHECKED_IN).
     * @param pageable The pagination information (page number, size, sort).
     * @return A Page of VisitorPass objects.
     */
    @Query("SELECT vp FROM VisitorPass vp WHERE vp.tenant.id = :tenantId AND DATE(vp.visitDateTime) = :date AND vp.status = :status")
    Page<VisitorPass> findTodaysVisitorsByTenantAndStatus(@Param("tenantId") Long tenantId, @Param("date") LocalDate date, @Param("status") PassStatus status, Pageable pageable);

    long countByTenantIdAndStatus(Long tenantId, PassStatus status);

    @Query("SELECT COUNT(vp) FROM VisitorPass vp WHERE vp.tenant.id = :tenantId AND DATE(vp.visitDateTime) = CURRENT_DATE AND vp.status = com.gt.visitor_pass_service.model.enums.PassStatus.APPROVED")
    long countApprovedForToday(@Param("tenantId") Long tenantId);

    @Query("SELECT COUNT(vp) FROM VisitorPass vp WHERE vp.tenant.id = :tenantId AND DATE(vp.visitDateTime) = CURRENT_DATE AND (vp.status = com.gt.visitor_pass_service.model.enums.PassStatus.CHECKED_OUT OR vp.status = com.gt.visitor_pass_service.model.enums.PassStatus.EXPIRED)")
    long countCompletedForToday(@Param("tenantId") Long tenantId);

    List<VisitorPass> findTop10ByTenantIdOrderByCreatedAtDesc(Long tenantId);

    @Query("SELECT COUNT(vp) FROM VisitorPass vp WHERE DATE(vp.visitDateTime) = CURRENT_DATE AND (vp.status = com.gt.visitor_pass_service.model.enums.PassStatus.APPROVED OR vp.status = com.gt.visitor_pass_service.model.enums.PassStatus.CHECKED_IN)")
    long countActivePassesForToday();

    long countByTenantId(Long tenantId);

    @Query("SELECT COUNT(vp) FROM VisitorPass vp WHERE vp.tenant.id = :tenantId AND DATE(vp.visitDateTime) = CURRENT_DATE")
    long countPassesForTenantToday(@Param("tenantId") Long tenantId);

    List<VisitorPass> findTop10ByOrderByCreatedAtDesc();
}