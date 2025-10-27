package com.example.demo.repo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.entity.TestCaseHeader;
import com.example.demo.entity.TestCaseTransaction;



public interface TestCaseTransactionRepository extends JpaRepository<TestCaseTransaction, String> {
    List<TestCaseTransaction> findByMainKey(String mainKey);
    List<TestCaseTransaction> findByTestCaseNo(String testCaseNo);
	TestCaseHeader save(TestCaseHeader header);


    
    
 
    
}

