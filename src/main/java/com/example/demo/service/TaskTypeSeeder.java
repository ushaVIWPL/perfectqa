package com.example.demo.service;

import com.example.demo.entity.TaskType;
import com.example.demo.repo.TaskTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskTypeSeeder implements CommandLineRunner {

    private final TaskTypeRepository taskTypeRepository;

    @Override
    public void run(String... args) throws Exception {
        List<String> defaultTypes = List.of(
            "Troubleshooting",
            "Infrastructure Config",
            "Application Config",
            "Report Modification",
            "New Report",
            "New Feature",
            "End-User Education",
            "Other"
        );

        for (String typeName : defaultTypes) {
            if (taskTypeRepository.findByName(typeName).isEmpty()) {
                TaskType taskType = new TaskType();
                taskType.setName(typeName);
                taskTypeRepository.save(taskType);
            }
        }
        System.out.println("✅ Task types seeded successfully!");
    }
}
