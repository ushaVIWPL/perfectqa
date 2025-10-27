package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.CityRef;


public interface CityRefRepository extends JpaRepository<CityRef, String> {
    // You can add custom queries if needed
}

