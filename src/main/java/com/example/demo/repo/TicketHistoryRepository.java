package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.TicketHistory;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {
    
    List<TicketHistory> findByTicketIdOrderByChangedAtDesc(Long ticketId);
    
    List<TicketHistory> findByChangedBy(String changedBy);
    
    List<TicketHistory> findByAction(String action);
}





















