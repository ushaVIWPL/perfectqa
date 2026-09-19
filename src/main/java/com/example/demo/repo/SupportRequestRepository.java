package com.example.demo.repo;

import com.example.demo.entity.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
    
    List<SupportRequest> findAllByOrderByCreatedAtDesc();
    
    List<SupportRequest> findByStatusOrderByCreatedAtDesc(String status);
    
    @Query("SELECT COUNT(s) FROM SupportRequest s WHERE s.status = 'PENDING'")
    long countPendingRequests();
    
    @Query("SELECT s FROM SupportRequest s WHERE s.status = :status ORDER BY s.createdAt DESC")
    List<SupportRequest> findByStatus(@Param("status") String status);
}

