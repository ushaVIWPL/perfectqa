package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entity.CompanyRef;
import com.example.demo.repo.CompanyRefRepository;

@Service
public class CompanyRefService {

    private final CompanyRefRepository repository;

    public CompanyRefService(CompanyRefRepository repository) {
        this.repository = repository;
    }

    public CompanyRef save(CompanyRef company) {
        return repository.save(company);
    }

    public List<CompanyRef> getAll() {
        return repository.findAll();
    }

    public Optional<CompanyRef> getById(String  code) {
        return repository.findById(code);
    }

    public void deleteById(String code) {
        repository.deleteById(code);
    }
    public CompanyRef findByCompanyCode(String companyCode) {
        return repository.findByCompanyCode(companyCode);
    }

}
