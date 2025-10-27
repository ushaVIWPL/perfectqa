package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.LanguageRef;

public interface LanguageRefRepository extends JpaRepository<LanguageRef, String> {

}
