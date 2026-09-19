package com.example.demo.embeddedid;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class TestCaseTransactionId implements Serializable {

    @Column(name = "test_case_no")
    private String testCaseNo;       // FK to testcase_headers.test_case_no

    @Column(name = "step_no")
    private String stepNo;           // 2-digit step number (e.g., "01", "02")

    @Column(name = "company_code")
    private String companyCode;

    public TestCaseTransactionId() {}

    public TestCaseTransactionId(String testCaseNo, String stepNo, String companyCode) {
        this.testCaseNo = testCaseNo;
        this.stepNo = stepNo;
        this.companyCode = companyCode;
    }
}
