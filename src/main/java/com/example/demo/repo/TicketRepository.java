package com.example.demo.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    // ==================== Non-deleted tickets (default behavior) ====================
    
    @Query("SELECT t FROM Ticket t WHERE t.companyCode = :companyCode AND (t.deleted = false OR t.deleted IS NULL)")
    List<Ticket> findByCompanyCode(@Param("companyCode") String companyCode);
    
    @Query("SELECT t FROM Ticket t WHERE t.companyCode = :companyCode AND t.departmentCode = :departmentCode AND (t.deleted = false OR t.deleted IS NULL) ORDER BY t.createdDate DESC")
    List<Ticket> findByCompanyCodeAndDepartmentCodeOrderByCreatedDateDesc(@Param("companyCode") String companyCode, @Param("departmentCode") String departmentCode);
    
    @Query("SELECT t FROM Ticket t WHERE t.companyCode = :companyCode AND t.departmentCode = :departmentCode AND (LOWER(REPLACE(t.issueType, ' ', '_')) = LOWER(REPLACE(:issueType, ' ', '_'))) AND (t.deleted = false OR t.deleted IS NULL) ORDER BY t.id DESC")
    List<Ticket> findByCompanyCodeAndDepartmentCodeAndIssueType(@Param("companyCode") String companyCode, @Param("departmentCode") String departmentCode, @Param("issueType") String issueType);
    
    @Query("SELECT t FROM Ticket t WHERE t.transactionId = :transactionId AND (t.deleted = false OR t.deleted IS NULL)")
    List<Ticket> findByTransactionId(@Param("transactionId") Long transactionId);
    
    @Query("SELECT t FROM Ticket t WHERE t.status = :status AND (t.deleted = false OR t.deleted IS NULL)")
    List<Ticket> findByStatus(@Param("status") String status);
    
    @Query("SELECT t FROM Ticket t WHERE t.transactionMainKey = :transactionMainKey AND (t.deleted = false OR t.deleted IS NULL)")
    List<Ticket> findByTransactionMainKey(@Param("transactionMainKey") String transactionMainKey);
    
    @Query("SELECT t FROM Ticket t WHERE t.assignedTo = :assignedTo AND (t.deleted = false OR t.deleted IS NULL)")
    List<Ticket> findByAssignedTo(@Param("assignedTo") String assignedTo);
    
    @Query("SELECT t FROM Ticket t WHERE t.createdBy = :createdBy AND (t.deleted = false OR t.deleted IS NULL)")
    List<Ticket> findByCreatedBy(@Param("createdBy") String createdBy);
    
    @Query("SELECT t FROM Ticket t WHERE t.priority = :priority AND (t.deleted = false OR t.deleted IS NULL)")
    List<Ticket> findByPriority(@Param("priority") String priority);

    /**
     * Find all tickets assigned to a user across all assignee slots.
     * Checks assigneeUserCode (slot 1), assignee2UserCode (slot 2),
     * assignee3UserCode (slot 3), and the legacy assignedTo field.
     */
    @Query("SELECT DISTINCT t FROM Ticket t WHERE (t.deleted = false OR t.deleted IS NULL) " +
           "AND (LOWER(t.assigneeUserCode) = LOWER(:userId) " +
           "     OR LOWER(t.assignee2UserCode) = LOWER(:userId) " +
           "     OR LOWER(t.assignee3UserCode) = LOWER(:userId) " +
           "     OR LOWER(t.assignedTo) = LOWER(:userId)) " +
           "ORDER BY t.id DESC")
    List<Ticket> findMyAssignedTickets(@Param("userId") String userId);

    /**
     * Find all tickets assigned to a user within a specific company.
     */
    @Query("SELECT DISTINCT t FROM Ticket t WHERE (t.deleted = false OR t.deleted IS NULL) " +
           "AND t.companyCode = :companyCode " +
           "AND (LOWER(t.assigneeUserCode) = LOWER(:userId) " +
           "     OR LOWER(t.assignee2UserCode) = LOWER(:userId) " +
           "     OR LOWER(t.assignee3UserCode) = LOWER(:userId) " +
           "     OR LOWER(t.assignedTo) = LOWER(:userId)) " +
           "ORDER BY t.id DESC")
    List<Ticket> findMyAssignedTicketsByCompany(@Param("userId") String userId, @Param("companyCode") String companyCode);
    
    Optional<Ticket> findByTicketNo(String ticketNo);
    
    @Query("SELECT t FROM Ticket t WHERE t.companyCode = :companyCode AND (t.deleted = false OR t.deleted IS NULL) ORDER BY t.id DESC")
    List<Ticket> findByCompanyCodeOrderByCreatedDateDesc(@Param("companyCode") String companyCode);
    
    @Query("SELECT t FROM Ticket t WHERE (t.deleted = false OR t.deleted IS NULL) ORDER BY t.id DESC")
    List<Ticket> findAllByOrderByCreatedDateDesc();
    
    @Query("SELECT t FROM Ticket t WHERE t.companyCode = ?1 AND t.status IN ('OPEN', 'ASSIGNED', 'IN_PROGRESS') AND (t.deleted = false OR t.deleted IS NULL)")
    List<Ticket> findActiveTicketsByCompanyCode(String companyCode);
    
    @Query("SELECT t FROM Ticket t WHERE t.companyCode = :companyCode " +
           "AND (LOWER(REPLACE(t.issueType, ' ', '_')) = LOWER(REPLACE(:issueType, ' ', '_'))) " +
           "AND (t.deleted = false OR t.deleted IS NULL) " +
           "ORDER BY t.id DESC")
    List<Ticket> findByCompanyCodeAndIssueType(@Param("companyCode") String companyCode, @Param("issueType") String issueType);

    @Query("SELECT t FROM Ticket t WHERE (LOWER(REPLACE(t.issueType, ' ', '_')) = LOWER(REPLACE(:issueType, ' ', '_'))) " +
           "AND (t.deleted = false OR t.deleted IS NULL) " +
           "ORDER BY t.id DESC")
    List<Ticket> findByIssueType(@Param("issueType") String issueType);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = ?1 AND (t.deleted = false OR t.deleted IS NULL)")
    long countByStatus(String status);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.companyCode = :companyCode AND t.status = :status AND (t.deleted = false OR t.deleted IS NULL)")
    long countByCompanyCodeAndStatus(@Param("companyCode") String companyCode, @Param("status") String status);
    
    // ==================== Deleted tickets ====================
    
    @Query("SELECT t FROM Ticket t WHERE t.deleted = true ORDER BY t.deletedDate DESC")
    List<Ticket> findAllDeleted();
    
    @Query("SELECT t FROM Ticket t WHERE t.deleted = true AND t.companyCode = :companyCode ORDER BY t.deletedDate DESC")
    List<Ticket> findDeletedByCompanyCode(@Param("companyCode") String companyCode);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.deleted = true")
    long countDeleted();
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.deleted = true AND t.companyCode = :companyCode")
    long countDeletedByCompanyCode(@Param("companyCode") String companyCode);
}
