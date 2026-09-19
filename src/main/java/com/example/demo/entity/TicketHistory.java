package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ticket_history")
public class TicketHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    private String action; // STATUS_CHANGE, ASSIGNED, COMMENT_ADDED, ATTACHMENT_ADDED, etc.

    private String oldValue;
    private String newValue;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String changedBy;
    
    private LocalDateTime changedAt;

    @PrePersist
    public void prePersist() {
        if (this.changedAt == null) {
            this.changedAt = LocalDateTime.now();
        }
    }
}





















