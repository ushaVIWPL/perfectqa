package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "module_ref")  // must match actual DB table

public class ModuleRef {
	
	@Id
    private String moduleKey;   // e.g., MOD001
	private String leadTester;
    private String tester1;
    private String tester2;
    private String tester3;
    private String tester4;
    private String tester5;
    private String tester6;
    private String tester7;
    private String tester8;

    // --- Date Fields ---
    private String moduleStartDateActual;
    private String moduleEndDateEst;
    private String moduleEndDateActual;

    // --- Module Details ---
    private String moduleStatus;
    private String moduleExcelLead;
    private String moduleBusinessLead;
    private String moduleBusinessAdvisor;
    private String moduleArchitect;
    private String moduleFunctionLead;
    private String moduleTechnicalLead;
    private String moduleDBA;
    private String moduleDev;

    // --- Getters and Setters ---

    
    @ManyToOne
    @JoinColumn(name = "app_code") // link to application_ref.app_code
    private ApplicationRef application;
    public String getModuleKey() {
        return moduleKey;
    }

    public void setModuleKey(String moduleKey) {
        this.moduleKey = moduleKey;
    }

    public ApplicationRef getApplication() {
        return application;
    }

    public void setApplication(ApplicationRef application) {
        this.application = application;
    }

    public String getLeadTester() {
        return leadTester;
    }

    public void setLeadTester(String leadTester) {
        this.leadTester = leadTester;
    }

    public String getTester1() { return tester1; }
    public void setTester1(String tester1) { this.tester1 = tester1; }

    public String getTester2() { return tester2; }
    public void setTester2(String tester2) { this.tester2 = tester2; }

    public String getTester3() { return tester3; }
    public void setTester3(String tester3) { this.tester3 = tester3; }

    public String getTester4() { return tester4; }
    public void setTester4(String tester4) { this.tester4 = tester4; }

    public String getTester5() { return tester5; }
    public void setTester5(String tester5) { this.tester5 = tester5; }

    public String getTester6() { return tester6; }
    public void setTester6(String tester6) { this.tester6 = tester6; }

    public String getTester7() { return tester7; }
    public void setTester7(String tester7) { this.tester7 = tester7; }

    public String getTester8() { return tester8; }
    public void setTester8(String tester8) { this.tester8 = tester8; }

    public String getModuleStartDateActual() {
        return moduleStartDateActual;
    }

    public void setModuleStartDateActual(String moduleStartDateActual2) {
        this.moduleStartDateActual = moduleStartDateActual2;
    }

    public String getModuleEndDateEst() {
        return moduleEndDateEst;
    }

    public void setModuleEndDateEst(String moduleEndDateEst2) {
        this.moduleEndDateEst = moduleEndDateEst2;
    }

    public String getModuleEndDateActual() {
        return moduleEndDateActual;
    }

    public void setModuleEndDateActual(String moduleEndDateActual2) {
        this.moduleEndDateActual = moduleEndDateActual2;
    }

    public String getModuleStatus() {
        return moduleStatus;
    }

    public void setModuleStatus(String moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    public String getModuleExcelLead() {
        return moduleExcelLead;
    }

    public void setModuleExcelLead(String moduleExcelLead) {
        this.moduleExcelLead = moduleExcelLead;
    }

    public String getModuleBusinessLead() {
        return moduleBusinessLead;
    }

    public void setModuleBusinessLead(String moduleBusinessLead) {
        this.moduleBusinessLead = moduleBusinessLead;
    }

    public String getModuleBusinessAdvisor() {
        return moduleBusinessAdvisor;
    }

    public void setModuleBusinessAdvisor(String moduleBusinessAdvisor) {
        this.moduleBusinessAdvisor = moduleBusinessAdvisor;
    }

    public String getModuleArchitect() {
        return moduleArchitect;
    }

    public void setModuleArchitect(String moduleArchitect) {
        this.moduleArchitect = moduleArchitect;
    }

    public String getModuleFunctionLead() {
        return moduleFunctionLead;
    }

    public void setModuleFunctionLead(String moduleFunctionLead) {
        this.moduleFunctionLead = moduleFunctionLead;
    }

    public String getModuleTechnicalLead() {
        return moduleTechnicalLead;
    }

    public void setModuleTechnicalLead(String moduleTechnicalLead) {
        this.moduleTechnicalLead = moduleTechnicalLead;
    }

    public String getModuleDBA() {
        return moduleDBA;
    }

    public void setModuleDBA(String moduleDBA) {
        this.moduleDBA = moduleDBA;
    }

    public String getModuleDev() {
        return moduleDev;
    }

    public void setModuleDev(String moduleDev) {
        this.moduleDev = moduleDev;
    }

	public Object getPlannedStartDate() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPlannedStartDate(Object plannedStartDate) {
		// TODO Auto-generated method stub
		
	}
}	
	
	
	


