package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.TicketComment;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    
    List<TicketComment> findByTicketIdOrderByCommentedAtDesc(Long ticketId);
    
    List<TicketComment> findByCommentedBy(String commentedBy);
}





















