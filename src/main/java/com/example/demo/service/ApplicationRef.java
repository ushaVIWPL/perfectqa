package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.ProjectRef;
import com.example.demo.repo.Applicationrefrepo;
import com.example.demo.repo.ProjectRefRepository;

@Service
public class ApplicationRef {
	
	@Autowired
	private Applicationrefrepo repo;
	
	  @Autowired
	    private ProjectRefRepository projectRepo;

	
	public String saveApplication(String projectCode, com.example.demo.entity.ApplicationRef appRef) {
        // Find the project by projectCode
        ProjectRef project = projectRepo.findById(projectCode)
                .orElseThrow(() -> new RuntimeException("Project not found with code: " + projectCode));

        // Set the project in application
        appRef.setProject(project);

        // Save the application
        repo.save(appRef);

        return "Application saved successfully under project: " + projectCode;
    }


}
