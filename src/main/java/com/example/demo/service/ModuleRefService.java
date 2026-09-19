package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.ModuleRef;
import com.example.demo.repo.ModuleRefRepository;

@Service
public class ModuleRefService {

    @Autowired
    private ModuleRefRepository moduleRefRepository;

    public ModuleRef saveModule(ModuleRef module) {
        return moduleRefRepository.save(module);
    }
    
 
    
    public List<ModuleRef> getAllModules() {
        return moduleRefRepository.findAll();
    }
    
    
    public Optional<ModuleRef> getModuleByKey(String moduleKey) {
        return moduleRefRepository.findById(moduleKey);
    }
    
    public boolean deleteModule(String moduleKey) {
        Optional<ModuleRef> moduleOpt = moduleRefRepository.findByModuleKey(moduleKey);
        if (moduleOpt.isPresent()) {
            moduleRefRepository.delete(moduleOpt.get());
            return true;  // ✅ Delete successful
        }
        return false;  // ❌ Module not found
    }
    
    // Get modules by company code
    public List<ModuleRef> getModulesByCompanyCode(String companyCode) {
        return moduleRefRepository.findByCompanyCode(companyCode);
    }
    
    // Get modules by application code
    public List<ModuleRef> getModulesByApplicationCode(String appCode) {
        return moduleRefRepository.findByApplicationCode(appCode);
    }
    
    // Get modules by project code
    public List<ModuleRef> getModulesByProjectCode(String projectCode) {
        return moduleRefRepository.findByProjectCode(projectCode);
    }
    
    // Search modules by company name (case-insensitive partial match)
    public List<ModuleRef> searchByCompanyName(String companyName) {
        if (companyName == null || companyName.trim().isEmpty()) {
            return getAllModules();
        }
        return moduleRefRepository.findByCompanyNameContainingIgnoreCase(companyName.trim());
    }
    
    // Search modules by company code (case-insensitive partial match)
    public List<ModuleRef> searchByCompanyCode(String companyCode) {
        if (companyCode == null || companyCode.trim().isEmpty()) {
            return getAllModules();
        }
        return moduleRefRepository.findByCompanyCodeContainingIgnoreCase(companyCode.trim());
    }
    
    // Search modules by company name or company code (case-insensitive partial match)
    public List<ModuleRef> searchByCompanyNameOrCode(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllModules();
        }
        return moduleRefRepository.findByCompanyNameOrCodeContainingIgnoreCase(searchTerm.trim());
    }

}
