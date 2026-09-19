package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.SupportIssueType;
import java.util.Optional;

public interface SupportIssueTypeRepository extends JpaRepository<SupportIssueType, Long> {
    Optional<SupportIssueType> findByName(String name);
}
