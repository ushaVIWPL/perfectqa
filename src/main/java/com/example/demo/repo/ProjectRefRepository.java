package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.ProjectRef;

public interface ProjectRefRepository extends JpaRepository<ProjectRef, String> {

}
