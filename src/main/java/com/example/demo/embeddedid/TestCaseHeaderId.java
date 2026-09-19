package com.example.demo.embeddedid;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class TestCaseHeaderId implements Serializable {

    @Column(name = "transaction_key")
    private String transactionKey;     // FK from scenario_activities

    @Column(name = "test_case_no")
    private String testCaseNo;         // 2-digit test case number

    @Column(name = "company_code")
    private String companyCode;        // company code
}
