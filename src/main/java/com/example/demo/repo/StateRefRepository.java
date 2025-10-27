package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.StateRef;

public interface StateRefRepository extends JpaRepository<StateRef, String> {
    StateRef findByStateSubdivisionCode(String stateSubdivisionCode); // ✅ CORRECT
}
