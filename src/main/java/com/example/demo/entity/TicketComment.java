package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ticket_comments")
public class TicketComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;

    private String commentedBy;
    
    private LocalDateTime commentedAt;

    // Optional attachment for comment
    private String attachmentPath;

    @PrePersist
    public void prePersist() {
        if (this.commentedAt == null) {
            this.commentedAt = LocalDateTime.now();
        }
    }
}





















