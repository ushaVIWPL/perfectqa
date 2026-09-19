package com.example.demo.service;

import com.example.demo.entity.Department;
import com.example.demo.repo.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    public List<Department> getAll() {
        return repository.findAll();
    }

    public List<Department> getByCompanyCode(String companyCode) {
        if (companyCode == null || companyCode.trim().isEmpty()) {
            return getAll();
        }
        String trimmed = companyCode.trim();
        List<Department> list = repository.findByCompany_CompanyCode(trimmed);
        if (list.isEmpty()) {
            list = repository.findByCompany_CompanyCodeIgnoreCase(trimmed);
        }
        if (list.isEmpty()) {
            List<Department> all = getAll();
            list = all.stream().filter(d -> d.getCompany() != null && d.getCompany().getCompanyCode() != null &&
                    (d.getCompany().getCompanyCode().equalsIgnoreCase(trimmed) ||
                     d.getCompany().getCompanyCode().toLowerCase().contains(trimmed.toLowerCase()) ||
                     trimmed.toLowerCase().contains(d.getCompany().getCompanyCode().toLowerCase())))
                    .toList();
            if (list.isEmpty()) {
                return all;
            }
        }
        return list;
    }

    public Optional<Department> getById(String departmentCode) {
        if (departmentCode == null) return Optional.empty();
        return repository.findById(departmentCode);
    }

    public Department save(Department department) {
        return repository.save(department);
    }

    public void deleteById(String departmentCode) {
        if (departmentCode != null && repository.existsById(departmentCode)) {
            repository.deleteById(departmentCode);
        }
    }

    public List<Department> search(String search, String companyCode) {
        if (search == null || search.trim().isEmpty()) {
            return getByCompanyCode(companyCode);
        }
        if (companyCode != null && !companyCode.trim().isEmpty()) {
            return repository.findByCompany_CompanyCodeAndDepartmentNameContainingIgnoreCase(companyCode, search.trim());
        }
        return repository.findByDepartmentNameContainingIgnoreCaseOrDepartmentCodeContainingIgnoreCase(search.trim(), search.trim());
    }
}
