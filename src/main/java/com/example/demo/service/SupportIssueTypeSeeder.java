package com.example.demo.service;

import com.example.demo.entity.SupportIssueType;
import com.example.demo.repo.SupportIssueTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SupportIssueTypeSeeder implements CommandLineRunner {

    private final SupportIssueTypeRepository supportIssueTypeRepository;

    @Override
    public void run(String... args) throws Exception {
        List<String> defaultTypes = List.of(
            "Hardware Issue",
            "Software Installation",
            "Network Connectivity",
            "Email / Account Access",
            "Printer / Peripheral Issue",
            "VPN / Remote Access",
            "System Performance",
            "Other"
        );

        for (String typeName : defaultTypes) {
            if (supportIssueTypeRepository.findByName(typeName).isEmpty()) {
                SupportIssueType issueType = new SupportIssueType();
                issueType.setName(typeName);
                supportIssueTypeRepository.save(issueType);
            }
        }
        System.out.println("✅ IT Support issue types seeded successfully!");
    }
}
