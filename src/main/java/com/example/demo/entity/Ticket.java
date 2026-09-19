package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.ToString;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
@ToString(exclude = {"comments", "history", "tasks"})
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== IDENTIFICATION ====================
    @Column(unique = true)
    private String ticketNo;
    private String issueCode;
    
    // ==================== COMPANY & PROJECT ====================
    private String companyCode;
    private String departmentCode;
    private String applicationCode;
    private String moduleCode;
    private String testCaseCode;
    private String testCaseTransactionCode;

    // ==================== ISSUE DETAILS ====================
    private String issueType;
    
    @Column(columnDefinition = "TEXT")
    private String issueDescription;
    
    @Column(columnDefinition = "TEXT")
    private String issueLongDescription;
    
    private String errorCode;
    private String status;
    private String priority;

    // ==================== CREATION INFO ====================
    private LocalDateTime datetime;
    private String userCode;
    
    // ==================== ATTACHMENTS ====================
    @Column(columnDefinition = "TEXT")
    private String attachments;
    private String attachmentPaths;

    // ==================== ASSIGNEE 1 ====================
    private String assigneeUserCode;
    private String assigneeName;
    private LocalDate assigneeExpectedDate1;
    
    @Column(columnDefinition = "TEXT")
    private String assignee1Notes;
    
    @Column(columnDefinition = "TEXT")
    private String assignee1Resolution;
    
    private LocalDate assignee1ResolvedDate;

    // ==================== ASSIGNEE 2 ====================
    private String assignee2UserCode;
    private LocalDate assignee2ExpectedDate;
    
    @Column(columnDefinition = "TEXT")
    private String assignee2Notes;
    
    @Column(columnDefinition = "TEXT")
    private String assignee2Resolution;
    
    private LocalDate assignee2ResolvedDate;

    // ==================== ASSIGNEE 3 ====================
    private String assignee3UserCode;
    private LocalDate assignee3ExpectedDate;
    
    @Column(columnDefinition = "TEXT")
    private String assignee3Notes;
    
    @Column(columnDefinition = "TEXT")
    private String assignee3Resolution;
    
    private LocalDate assignee3ResolvedDate;

    // ==================== ADDITIONAL COMMENTS ====================
    @Column(columnDefinition = "TEXT")
    private String additionalComments;

    // ==================== TICKET CLOSURE (First Close) ====================
    private LocalDate ticketClosedDate;
    private String ticketClosedName;
    
    @Column(columnDefinition = "TEXT")
    private String closingNotes;

    // ==================== TICKET REOPEN ====================
    private LocalDate ticketReopenDate;
    private String ticketReopenUserName;
    
    @Column(columnDefinition = "TEXT")
    private String reopenReason;

    // ==================== TICKET CLOSURE 2 (Second Close after Reopen) ====================
    private LocalDate closedDate2;
    private String closedBy2Name;
    
    @Column(columnDefinition = "TEXT")
    private String closedDetails2;
    
    @Column(columnDefinition = "TEXT")
    private String closed2Comments;

    // ==================== SOFT DELETE FIELDS ====================
    private Boolean deleted = false;
    private LocalDateTime deletedDate;
    private String deletedBy;

    // ==================== LEGACY FIELDS ====================
    private String projectName;
    private String module;
    private String feature;
    private String summary;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(columnDefinition = "TEXT")
    private String stepsToReproduce;
    
    @Column(columnDefinition = "TEXT")
    private String expectedResult;
    
    @Column(columnDefinition = "TEXT")
    private String actualResult;
    
    private Long transactionId;
    private String transactionMainKey;
    private String createdBy;
    private String assignedTo;
    private LocalDate createdDate;
    private LocalDateTime createdAt;
    private LocalDate assignedDate;
    private LocalDate resolvedDate;
    private LocalDate closedDate;
    private LocalDate reopenedDate;
    private LocalDate committedDate;
    
    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;
    
    private String resolvedBy;
    private String closedBy;
    private String reopenedBy;

    // ==================== RELATIONSHIPS ====================
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketHistory> history = new ArrayList<>();

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketTask> tasks = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.datetime == null) this.datetime = LocalDateTime.now();
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.createdDate == null) this.createdDate = LocalDate.now();
        if (this.status == null) this.status = "OPEN";
        if (this.deleted == null) this.deleted = false;
    }

    // GETTERS AND SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTicketNo() { return ticketNo; }
    public void setTicketNo(String ticketNo) { this.ticketNo = ticketNo; }
    public String getIssueCode() { return issueCode; }
    public void setIssueCode(String issueCode) { this.issueCode = issueCode; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public String getApplicationCode() { return applicationCode; }
    public void setApplicationCode(String applicationCode) { this.applicationCode = applicationCode; }
    public String getModuleCode() { return moduleCode; }
    public void setModuleCode(String moduleCode) { this.moduleCode = moduleCode; }
    public String getTestCaseCode() { return testCaseCode; }
    public void setTestCaseCode(String testCaseCode) { this.testCaseCode = testCaseCode; }
    public String getTestCaseTransactionCode() { return testCaseTransactionCode; }
    public void setTestCaseTransactionCode(String testCaseTransactionCode) { this.testCaseTransactionCode = testCaseTransactionCode; }
    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    public String getIssueDescription() { return issueDescription; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }
    public String getIssueLongDescription() { return issueLongDescription; }
    public void setIssueLongDescription(String issueLongDescription) { this.issueLongDescription = issueLongDescription; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public LocalDateTime getDatetime() { return datetime; }
    public void setDatetime(LocalDateTime datetime) { this.datetime = datetime; }
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }
    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }
    public String getAttachmentPaths() { return attachmentPaths; }
    public void setAttachmentPaths(String attachmentPaths) { this.attachmentPaths = attachmentPaths; }
    public String getAssigneeUserCode() { return assigneeUserCode; }
    public void setAssigneeUserCode(String assigneeUserCode) { this.assigneeUserCode = assigneeUserCode; }
    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    public LocalDate getAssigneeExpectedDate1() { return assigneeExpectedDate1; }
    public void setAssigneeExpectedDate1(LocalDate assigneeExpectedDate1) { this.assigneeExpectedDate1 = assigneeExpectedDate1; }
    public String getAssignee1Notes() { return assignee1Notes; }
    public void setAssignee1Notes(String assignee1Notes) { this.assignee1Notes = assignee1Notes; }
    public String getAssignee1Resolution() { return assignee1Resolution; }
    public void setAssignee1Resolution(String assignee1Resolution) { this.assignee1Resolution = assignee1Resolution; }
    public LocalDate getAssignee1ResolvedDate() { return assignee1ResolvedDate; }
    public void setAssignee1ResolvedDate(LocalDate assignee1ResolvedDate) { this.assignee1ResolvedDate = assignee1ResolvedDate; }
    public String getAssignee2UserCode() { return assignee2UserCode; }
    public void setAssignee2UserCode(String assignee2UserCode) { this.assignee2UserCode = assignee2UserCode; }
    public LocalDate getAssignee2ExpectedDate() { return assignee2ExpectedDate; }
    public void setAssignee2ExpectedDate(LocalDate assignee2ExpectedDate) { this.assignee2ExpectedDate = assignee2ExpectedDate; }
    public String getAssignee2Notes() { return assignee2Notes; }
    public void setAssignee2Notes(String assignee2Notes) { this.assignee2Notes = assignee2Notes; }
    public String getAssignee2Resolution() { return assignee2Resolution; }
    public void setAssignee2Resolution(String assignee2Resolution) { this.assignee2Resolution = assignee2Resolution; }
    public LocalDate getAssignee2ResolvedDate() { return assignee2ResolvedDate; }
    public void setAssignee2ResolvedDate(LocalDate assignee2ResolvedDate) { this.assignee2ResolvedDate = assignee2ResolvedDate; }
    public String getAssignee3UserCode() { return assignee3UserCode; }
    public void setAssignee3UserCode(String assignee3UserCode) { this.assignee3UserCode = assignee3UserCode; }
    public LocalDate getAssignee3ExpectedDate() { return assignee3ExpectedDate; }
    public void setAssignee3ExpectedDate(LocalDate assignee3ExpectedDate) { this.assignee3ExpectedDate = assignee3ExpectedDate; }
    public String getAssignee3Notes() { return assignee3Notes; }
    public void setAssignee3Notes(String assignee3Notes) { this.assignee3Notes = assignee3Notes; }
    public String getAssignee3Resolution() { return assignee3Resolution; }
    public void setAssignee3Resolution(String assignee3Resolution) { this.assignee3Resolution = assignee3Resolution; }
    public LocalDate getAssignee3ResolvedDate() { return assignee3ResolvedDate; }
    public void setAssignee3ResolvedDate(LocalDate assignee3ResolvedDate) { this.assignee3ResolvedDate = assignee3ResolvedDate; }
    public String getAdditionalComments() { return additionalComments; }
    public void setAdditionalComments(String additionalComments) { this.additionalComments = additionalComments; }
    public LocalDate getTicketClosedDate() { return ticketClosedDate; }
    public void setTicketClosedDate(LocalDate ticketClosedDate) { this.ticketClosedDate = ticketClosedDate; }
    public String getTicketClosedName() { return ticketClosedName; }
    public void setTicketClosedName(String ticketClosedName) { this.ticketClosedName = ticketClosedName; }
    public String getClosingNotes() { return closingNotes; }
    public void setClosingNotes(String closingNotes) { this.closingNotes = closingNotes; }
    public LocalDate getTicketReopenDate() { return ticketReopenDate; }
    public void setTicketReopenDate(LocalDate ticketReopenDate) { this.ticketReopenDate = ticketReopenDate; }
    public String getTicketReopenUserName() { return ticketReopenUserName; }
    public void setTicketReopenUserName(String ticketReopenUserName) { this.ticketReopenUserName = ticketReopenUserName; }
    public String getReopenReason() { return reopenReason; }
    public void setReopenReason(String reopenReason) { this.reopenReason = reopenReason; }
    public LocalDate getClosedDate2() { return closedDate2; }
    public void setClosedDate2(LocalDate closedDate2) { this.closedDate2 = closedDate2; }
    public String getClosedBy2Name() { return closedBy2Name; }
    public void setClosedBy2Name(String closedBy2Name) { this.closedBy2Name = closedBy2Name; }
    public String getClosedDetails2() { return closedDetails2; }
    public void setClosedDetails2(String closedDetails2) { this.closedDetails2 = closedDetails2; }
    public String getClosed2Comments() { return closed2Comments; }
    public void setClosed2Comments(String closed2Comments) { this.closed2Comments = closed2Comments; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getDeletedDate() { return deletedDate; }
    public void setDeletedDate(LocalDateTime deletedDate) { this.deletedDate = deletedDate; }
    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getFeature() { return feature; }
    public void setFeature(String feature) { this.feature = feature; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStepsToReproduce() { return stepsToReproduce; }
    public void setStepsToReproduce(String stepsToReproduce) { this.stepsToReproduce = stepsToReproduce; }
    public String getExpectedResult() { return expectedResult; }
    public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }
    public String getActualResult() { return actualResult; }
    public void setActualResult(String actualResult) { this.actualResult = actualResult; }
    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }
    public String getTransactionMainKey() { return transactionMainKey; }
    public void setTransactionMainKey(String transactionMainKey) { this.transactionMainKey = transactionMainKey; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDate getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDate assignedDate) { this.assignedDate = assignedDate; }
    public LocalDate getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(LocalDate resolvedDate) { this.resolvedDate = resolvedDate; }
    public LocalDate getClosedDate() { return closedDate; }
    public void setClosedDate(LocalDate closedDate) { this.closedDate = closedDate; }
    public LocalDate getReopenedDate() { return reopenedDate; }
    public void setReopenedDate(LocalDate reopenedDate) { this.reopenedDate = reopenedDate; }
    public LocalDate getCommittedDate() { return committedDate; }
    public void setCommittedDate(LocalDate committedDate) { this.committedDate = committedDate; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    public String getClosedBy() { return closedBy; }
    public void setClosedBy(String closedBy) { this.closedBy = closedBy; }
    public String getReopenedBy() { return reopenedBy; }
    public void setReopenedBy(String reopenedBy) { this.reopenedBy = reopenedBy; }
    public List<TicketComment> getComments() { return comments; }
    public void setComments(List<TicketComment> comments) { this.comments = comments; }
    public List<TicketHistory> getHistory() { return history; }
    public void setHistory(List<TicketHistory> history) { this.history = history; }
    public List<TicketTask> getTasks() { return tasks; }
    public void setTasks(List<TicketTask> tasks) { this.tasks = tasks; }

    public List<String> getAttachmentList() {
        List<String> paths = new ArrayList<>();
        String pathsStr = attachments != null ? attachments : attachmentPaths;
        if (pathsStr != null && !pathsStr.isEmpty()) {
            String[] split = pathsStr.split(",");
            for (String path : split) {
                if (path != null && !path.trim().isEmpty()) {
                    paths.add(path.trim());
                }
            }
        }
        return paths;
    }
}
