package com.example.demo.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.ProjectRef;
import com.example.demo.repo.ProjectRefRepository;

@Service
public class ProjectRefService {
	
	/*
	 * private final ProjectRefRepository repository;
	 */	 
	   @Autowired
	    private ProjectRefRepository repository;

	    public ProjectRefService(ProjectRefRepository repository) {
	        this.repository = repository;
	    }

	    public ProjectRef save(ProjectRef project) {
	        return repository.save(project);
	    }

	
	    public Optional<ProjectRef> getById(String id) {
	        return repository.findById(id);
	    }
	    
	 


		public void deleteById(String projectCode) {
	    	repository.deleteById(projectCode);
			
		}
		public List<ProjectRef> getAll() {
	        return repository.findAll();
	    }
}