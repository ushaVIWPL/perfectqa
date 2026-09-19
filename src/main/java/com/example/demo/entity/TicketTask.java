package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "ticket_tasks")
public class TicketTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    private String companyCode;

    private String taskType;
    
    @Column(columnDefinition = "TEXT")
    private String descriptionOfWorkDone;
    
    private Double billableHours;
    
    private LocalDate startDate;
    private LocalDate completedDate;
    private LocalDate committedDate;
    
    private String performedBy;
    private String requestorTask;
    
    @Column(columnDefinition = "TEXT")
    private String comments;
    
    private String status; // OPEN, CLOSED
    
    private String lastModifiedBy;
    private java.time.LocalDateTime lastModifiedAt;
}
