package com.example.demo.repo;


import com.example.demo.entity.CountryRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryRefRepository extends JpaRepository<CountryRef, String> {
}
