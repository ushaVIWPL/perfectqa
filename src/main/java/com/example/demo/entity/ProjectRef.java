package com.example.demo.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class ProjectRef {

    @Id
    private String projectCode;

    private String projectName;
    private String execSponsor;
    private String busSponsor;
    private String budgetLead;
    private String businessGoal;
    private String functionalGoal;
    private String financialGoal;
    private String efficiencyGoal;
    private String qaGoal;
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String projectDescription;

    @ManyToOne
    @JoinColumn(name = "companyCode")
    private CompanyRef company;

    // ✅ Corrected OneToMany mapping
    @OneToMany(mappedBy = "project") // 'project' matches the field name in ApplicationRef
    private List<ApplicationRef> applications;

    // --- Getters and Setters ---
    public CompanyRef getCompany() {
        return company;
    }

    public void setCompany(CompanyRef company) {
        this.company = company;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getExecSponsor() {
        return execSponsor;
    }

    public void setExecSponsor(String execSponsor) {
        this.execSponsor = execSponsor;
    }

    public String getBusSponsor() {
        return busSponsor;
    }

    public void setBusSponsor(String busSponsor) {
        this.busSponsor = busSponsor;
    }

    public String getBudgetLead() {
        return budgetLead;
    }

    public void setBudgetLead(String budgetLead) {
        this.budgetLead = budgetLead;
    }

    public String getBusinessGoal() {
        return businessGoal;
    }

    public void setBusinessGoal(String businessGoal) {
        this.businessGoal = businessGoal;
    }

    public String getFunctionalGoal() {
        return functionalGoal;
    }

    public void setFunctionalGoal(String functionalGoal) {
        this.functionalGoal = functionalGoal;
    }

    public String getFinancialGoal() {
        return financialGoal;
    }

    public void setFinancialGoal(String financialGoal) {
        this.financialGoal = financialGoal;
    }

    public String getEfficiencyGoal() {
        return efficiencyGoal;
    }

    public void setEfficiencyGoal(String efficiencyGoal) {
        this.efficiencyGoal = efficiencyGoal;
    }

    public String getQaGoal() {
        return qaGoal;
    }

    public void setQaGoal(String qaGoal) {
        this.qaGoal = qaGoal;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public List<ApplicationRef> getApplications() {
        return applications;
    }

    public void setApplications(List<ApplicationRef> applications) {
        this.applications = applications;
    }
}
