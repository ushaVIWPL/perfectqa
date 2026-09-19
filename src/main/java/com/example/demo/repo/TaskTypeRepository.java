package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.TaskType;
import java.util.Optional;

public interface TaskTypeRepository extends JpaRepository<TaskType, Long> {
    Optional<TaskType> findByName(String name);
}
