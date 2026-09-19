package com.example.demo.service;

import com.example.demo.entity.SupportRequest;
import com.example.demo.repo.SupportRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupportRequestService {

    private final SupportRequestRepository repository;

    public SupportRequest createSupportRequest(SupportRequest request) {
        return repository.save(request);
    }

    public List<SupportRequest> getAllRequests() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public List<SupportRequest> getPendingRequests() {
        return repository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    public List<SupportRequest> getRequestsByStatus(String status) {
        return repository.findByStatus(status);
    }

    public Optional<SupportRequest> getById(Long id) {
        return repository.findById(id);
    }

    @Transactional
    public SupportRequest markAsContacted(Long id, String contactedBy, String adminNotes) {
        SupportRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Support request not found"));
        
        request.setStatus("CONTACTED");
        request.setContactedAt(LocalDateTime.now());
        request.setContactedBy(contactedBy);
        if (adminNotes != null && !adminNotes.trim().isEmpty()) {
            request.setAdminNotes(adminNotes);
        }
        
        return repository.save(request);
    }

    @Transactional
    public SupportRequest updateStatus(Long id, String status, String adminNotes) {
        SupportRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Support request not found"));
        
        request.setStatus(status);
        if (adminNotes != null && !adminNotes.trim().isEmpty()) {
            request.setAdminNotes(adminNotes);
        }
        
        if ("CONTACTED".equals(status) && request.getContactedAt() == null) {
            request.setContactedAt(LocalDateTime.now());
        }
        
        return repository.save(request);
    }

    public long countPendingRequests() {
        return repository.countPendingRequests();
    }

    public void deleteRequest(Long id) {
        repository.deleteById(id);
    }
}

