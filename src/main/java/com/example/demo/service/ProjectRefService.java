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
		
		// Get projects by company code
		public List<ProjectRef> getByCompanyCode(String companyCode) {
			return repository.findByCompanyCode(companyCode);
		}
		
		// Search projects by company name (case-insensitive partial match)
		public List<ProjectRef> searchByCompanyName(String companyName) {
			if (companyName == null || companyName.trim().isEmpty()) {
				return getAll();
			}
			return repository.findByCompanyNameContainingIgnoreCase(companyName.trim());
		}
		
		// Search projects by company code (case-insensitive partial match)
		public List<ProjectRef> searchByCompanyCode(String companyCode) {
			if (companyCode == null || companyCode.trim().isEmpty()) {
				return getAll();
			}
			return repository.findByCompanyCodeContainingIgnoreCase(companyCode.trim());
		}
		
		// Search projects by company name or company code (case-insensitive partial match)
		public List<ProjectRef> searchByCompanyNameOrCode(String searchTerm) {
			if (searchTerm == null || searchTerm.trim().isEmpty()) {
				return getAll();
			}
			return repository.findByCompanyNameOrCodeContainingIgnoreCase(searchTerm.trim());
		}
}