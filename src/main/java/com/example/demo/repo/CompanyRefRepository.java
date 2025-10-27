package com.example.demo.repo;

import com.example.demo.entity.CompanyRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRefRepository extends JpaRepository<CompanyRef, String> {
}
