package com.example.demo.repo;

import com.example.demo.entity.TicketTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TicketTaskRepository extends JpaRepository<TicketTask, Long> {
    List<TicketTask> findByTicketId(Long ticketId);
    
    @Query("SELECT t FROM TicketTask t WHERE (t.companyCode = :companyCode OR t.ticket.companyCode = :companyCode) AND ((t.startDate BETWEEN :start AND :end) OR (t.completedDate BETWEEN :start AND :end) OR (t.startDate IS NULL AND t.completedDate IS NULL))")
    List<TicketTask> findByCompanyCodeAndDates(
            @Param("companyCode") String companyCode, 
            @Param("start") LocalDate start, 
            @Param("end") LocalDate end);

    List<TicketTask> findByPerformedByAndStatusNot(String performedBy, String status);
    
    List<TicketTask> findByCompanyCode(String companyCode);
    List<TicketTask> findByPerformedBy(String performedBy);
    List<TicketTask> findByPerformedByIgnoreCase(String performedBy);
    
    @Query("SELECT COUNT(t) FROM TicketTask t WHERE (:companyCode IS NULL OR t.companyCode = :companyCode) AND UPPER(t.status) = UPPER(:status)")
    long countByCompanyAndStatus(@Param("companyCode") String companyCode, @Param("status") String status);

    @Query("SELECT COUNT(t) FROM TicketTask t WHERE (:companyCode IS NULL OR t.companyCode = :companyCode) AND UPPER(t.status) IN ('OPEN', 'IN_PROGRESS', 'ASSIGNED', 'REOPENED')")
    long countInProgressByCompany(@Param("companyCode") String companyCode);

    @Query("SELECT t FROM TicketTask t LEFT JOIN FETCH t.ticket WHERE t.status IS NOT NULL AND UPPER(t.status) IN ('OPEN', 'IN_PROGRESS', 'ASSIGNED', 'REOPENED')")
    List<TicketTask> findActiveTasks();
}
