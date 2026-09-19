package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.demo.entity.BusinessScenario;
import com.example.demo.entity.ScenarioActivities;
import com.example.demo.entity.TestCaseHeader;
import com.example.demo.entity.TestCaseTransaction;
import com.example.demo.entity.TicketTask;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class ReportExportService {

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.demo.service.UserAccountService userAccountService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.example.demo.repo.CompanyRefRepository companyRefRepository;

    // Excel Export for Business Scenarios
    public byte[] exportBusinessScenariosToExcel(List<BusinessScenario> scenarios, String companyCode) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Business Scenarios");
        
        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        // Create header row
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        String[] headers = {"Business Scenario", "Activity", "Expected Outcome", "Responsible", "Work Stream", "Description", "T-Code"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Add data rows
        int rowNum = 1;
        for (BusinessScenario scenario : scenarios) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(scenario.getId().getBusinessScenario());
            row.createCell(1).setCellValue(scenario.getActivity() != null ? scenario.getActivity() : "");
            row.createCell(2).setCellValue(scenario.getExpectedOutcome() != null ? scenario.getExpectedOutcome() : "");
            row.createCell(3).setCellValue(scenario.getResponsible() != null ? scenario.getResponsible() : "");
            row.createCell(4).setCellValue(scenario.getWorkStream() != null ? scenario.getWorkStream() : "");
            row.createCell(5).setCellValue(scenario.getScenarioDescription() != null ? scenario.getScenarioDescription() : "");
            row.createCell(6).setCellValue(scenario.getTcode() != null ? scenario.getTcode() : "");
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    // PDF Export for Business Scenarios
    public byte[] exportBusinessScenariosToPDF(List<BusinessScenario> scenarios, String companyCode) throws DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // Title
        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Business Scenarios Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        
        // Company Code
        com.lowagie.text.Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph info = new Paragraph("Company Code: " + companyCode + " | Generated: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), infoFont);
        info.setSpacingAfter(15);
        document.add(info);
        
        // Table
        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 2f, 2.5f, 1.5f, 1.5f, 3f, 1f});
        
        // Header
        String[] headers = {"Business Scenario", "Activity", "Expected Outcome", "Responsible", "Work Stream", "Description", "T-Code"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            cell.setBackgroundColor(new java.awt.Color(102, 126, 234));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
        
        // Data
        for (BusinessScenario scenario : scenarios) {
            table.addCell(new Phrase(scenario.getId().getBusinessScenario() != null ? scenario.getId().getBusinessScenario() : ""));
            table.addCell(new Phrase(scenario.getActivity() != null ? scenario.getActivity() : ""));
            table.addCell(new Phrase(scenario.getExpectedOutcome() != null ? scenario.getExpectedOutcome() : ""));
            table.addCell(new Phrase(scenario.getResponsible() != null ? scenario.getResponsible() : ""));
            table.addCell(new Phrase(scenario.getWorkStream() != null ? scenario.getWorkStream() : ""));
            table.addCell(new Phrase(scenario.getScenarioDescription() != null ? scenario.getScenarioDescription() : ""));
            table.addCell(new Phrase(scenario.getTcode() != null ? scenario.getTcode() : ""));
        }
        
        document.add(table);
        document.close();
        
        return outputStream.toByteArray();
    }

    // Excel Export for Test Case Headers (simplified version)
    public byte[] exportTestCaseHeadersToExcel(Map<String, Map<String, List<TestCaseHeader>>> reportData, String companyCode) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        for (Map.Entry<String, Map<String, List<TestCaseHeader>>> scenarioEntry : reportData.entrySet()) {
            Sheet sheet = workbook.createSheet("BS-" + scenarioEntry.getKey());
            int rowNum = 0;
            
            // Scenario header
            org.apache.poi.ss.usermodel.Row scenarioRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell scenarioCell = scenarioRow.createCell(0);
            scenarioCell.setCellValue("Business Scenario: " + scenarioEntry.getKey());
            CellStyle scenarioStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font scenarioFont = workbook.createFont();
            scenarioFont.setBold(true);
            scenarioFont.setFontHeightInPoints((short) 14);
            scenarioStyle.setFont(scenarioFont);
            scenarioCell.setCellStyle(scenarioStyle);
            
            for (Map.Entry<String, List<TestCaseHeader>> activityEntry : scenarioEntry.getValue().entrySet()) {
                rowNum++; // Empty row
                org.apache.poi.ss.usermodel.Row activityRow = sheet.createRow(rowNum++);
                org.apache.poi.ss.usermodel.Cell activityCell = activityRow.createCell(0);
                activityCell.setCellValue("Transaction Key: " + activityEntry.getKey());
                activityCell.setCellStyle(scenarioStyle);
                
                // Table headers
                org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
                String[] headers = {"Combined Key", "Test Case No", "Activity", "Description", "Expected Outcome", "Responsible", "Work Stream", "T-Code"};
                for (int i = 0; i < headers.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }
                
                // Data rows
                for (TestCaseHeader header : activityEntry.getValue()) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(header.getCombinedKey() != null ? header.getCombinedKey() : "");
                    row.createCell(1).setCellValue(header.getTestCaseNo() != null ? header.getTestCaseNo() : "");
                    row.createCell(2).setCellValue(header.getActivity() != null ? header.getActivity() : "");
                    row.createCell(3).setCellValue(header.getDescription() != null ? header.getDescription() : "");
                    row.createCell(4).setCellValue(header.getExpectedOutcome() != null ? header.getExpectedOutcome() : "");
                    row.createCell(5).setCellValue(header.getResponsible() != null ? header.getResponsible() : "");
                    row.createCell(6).setCellValue(header.getWorkStream() != null ? header.getWorkStream() : "");
                    row.createCell(7).setCellValue(header.getTCode() != null ? header.getTCode() : "");
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    // PDF Export for Test Case Headers (simplified)
    public byte[] exportTestCaseHeadersToPDF(Map<String, Map<String, List<TestCaseHeader>>> reportData, String companyCode) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Test Case Headers Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        
        com.lowagie.text.Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph info = new Paragraph("Company Code: " + companyCode, infoFont);
        info.setSpacingAfter(15);
        document.add(info);
        
        for (Map.Entry<String, Map<String, List<TestCaseHeader>>> scenarioEntry : reportData.entrySet()) {
            Paragraph scenarioPara = new Paragraph("Business Scenario: " + scenarioEntry.getKey(), 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            scenarioPara.setSpacingAfter(10);
            document.add(scenarioPara);
            
            for (Map.Entry<String, List<TestCaseHeader>> activityEntry : scenarioEntry.getValue().entrySet()) {
                Paragraph activityPara = new Paragraph("Transaction Key: " + activityEntry.getKey(), 
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
                activityPara.setSpacingAfter(5);
                document.add(activityPara);
                
                PdfPTable table = new PdfPTable(8);
                table.setWidthPercentage(100);
                
                String[] headers = {"Combined Key", "Test Case No", "Activity", "Description", "Expected Outcome", "Responsible", "Work Stream", "T-Code"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
                    cell.setBackgroundColor(new java.awt.Color(40, 167, 69));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(3);
                    table.addCell(cell);
                }
                
                for (TestCaseHeader header : activityEntry.getValue()) {
                    table.addCell(new Phrase(header.getCombinedKey() != null ? header.getCombinedKey() : ""));
                    table.addCell(new Phrase(header.getTestCaseNo() != null ? header.getTestCaseNo() : ""));
                    table.addCell(new Phrase(header.getActivity() != null ? header.getActivity() : ""));
                    table.addCell(new Phrase(header.getDescription() != null ? header.getDescription() : ""));
                    table.addCell(new Phrase(header.getExpectedOutcome() != null ? header.getExpectedOutcome() : ""));
                    table.addCell(new Phrase(header.getResponsible() != null ? header.getResponsible() : ""));
                    table.addCell(new Phrase(header.getWorkStream() != null ? header.getWorkStream() : ""));
                    table.addCell(new Phrase(header.getTCode() != null ? header.getTCode() : ""));
                }
                
                document.add(table);
                document.add(new Paragraph(" ")); // Spacing
            }
        }
        
        document.close();
        return outputStream.toByteArray();
    }

    // Excel Export for Transactions (simplified)
    public byte[] exportTransactionsToExcel(Map<String, Map<String, Map<String, List<TestCaseTransaction>>>> reportData, String companyCode) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        Sheet sheet = workbook.createSheet("Transactions");
        int rowNum = 0;
        
        for (Map.Entry<String, Map<String, Map<String, List<TestCaseTransaction>>>> scenarioEntry : reportData.entrySet()) {
            org.apache.poi.ss.usermodel.Row scenarioRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell scenarioCell = scenarioRow.createCell(0);
            scenarioCell.setCellValue("Business Scenario: " + scenarioEntry.getKey());
            CellStyle scenarioStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font scenarioFont = workbook.createFont();
            scenarioFont.setBold(true);
            scenarioFont.setFontHeightInPoints((short) 14);
            scenarioStyle.setFont(scenarioFont);
            scenarioCell.setCellStyle(scenarioStyle);
            
            for (Map.Entry<String, Map<String, List<TestCaseTransaction>>> activityEntry : scenarioEntry.getValue().entrySet()) {
                rowNum++;
                org.apache.poi.ss.usermodel.Row activityRow = sheet.createRow(rowNum++);
                org.apache.poi.ss.usermodel.Cell activityCell = activityRow.createCell(0);
                activityCell.setCellValue("Transaction Key: " + activityEntry.getKey());
                activityCell.setCellStyle(scenarioStyle);
                
                for (Map.Entry<String, List<TestCaseTransaction>> headerEntry : activityEntry.getValue().entrySet()) {
                    rowNum++;
                    org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
                    org.apache.poi.ss.usermodel.Cell headerCell = headerRow.createCell(0);
                    headerCell.setCellValue("Test Case Header: " + headerEntry.getKey());
                    headerCell.setCellStyle(scenarioStyle);
                    
                    // Table headers
                    org.apache.poi.ss.usermodel.Row tableHeaderRow = sheet.createRow(rowNum++);
                    String[] headers = {"Main Key", "Step No", "Action", "Expected Results", "Actual Results", "Pass/Fail", "Defects", "Comments", "Tested Date"};
                    for (int i = 0; i < headers.length; i++) {
                        org.apache.poi.ss.usermodel.Cell cell = tableHeaderRow.createCell(i);
                        cell.setCellValue(headers[i]);
                        cell.setCellStyle(headerStyle);
                    }
                    
                    // Data rows
                    for (TestCaseTransaction transaction : headerEntry.getValue()) {
                        org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(transaction.getMainKey() != null ? transaction.getMainKey() : "");
                        row.createCell(1).setCellValue(transaction.getStepNo() != null ? transaction.getStepNo() : "");
                        row.createCell(2).setCellValue(transaction.getAction() != null ? transaction.getAction() : "");
                        row.createCell(3).setCellValue(transaction.getExpectedResults() != null ? transaction.getExpectedResults() : "");
                        row.createCell(4).setCellValue(transaction.getActualResults() != null ? transaction.getActualResults() : "");
                        row.createCell(5).setCellValue(transaction.getPassFail() != null ? transaction.getPassFail() : "");
                        row.createCell(6).setCellValue(transaction.getDefects() != null ? transaction.getDefects() : "");
                        row.createCell(7).setCellValue(transaction.getComments() != null ? transaction.getComments() : "");
                        row.createCell(8).setCellValue(transaction.getTestedDate() != null ? transaction.getTestedDate().toString() : "");
                    }
                }
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < 9; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    // PDF Export for Transactions (simplified)
    public byte[] exportTransactionsToPDF(Map<String, Map<String, Map<String, List<TestCaseTransaction>>>> reportData, String companyCode) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Transactions Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        
        com.lowagie.text.Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph info = new Paragraph("Company Code: " + companyCode, infoFont);
        info.setSpacingAfter(15);
        document.add(info);
        
        for (Map.Entry<String, Map<String, Map<String, List<TestCaseTransaction>>>> scenarioEntry : reportData.entrySet()) {
            Paragraph scenarioPara = new Paragraph("Business Scenario: " + scenarioEntry.getKey(), 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            scenarioPara.setSpacingAfter(10);
            document.add(scenarioPara);
            
            for (Map.Entry<String, Map<String, List<TestCaseTransaction>>> activityEntry : scenarioEntry.getValue().entrySet()) {
                Paragraph activityPara = new Paragraph("Transaction Key: " + activityEntry.getKey(), 
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
                activityPara.setSpacingAfter(5);
                document.add(activityPara);
                
                for (Map.Entry<String, List<TestCaseTransaction>> headerEntry : activityEntry.getValue().entrySet()) {
                    Paragraph headerPara = new Paragraph("Test Case Header: " + headerEntry.getKey(), 
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10));
                    headerPara.setSpacingAfter(5);
                    document.add(headerPara);
                    
                    PdfPTable table = new PdfPTable(9);
                    table.setWidthPercentage(100);
                    
                    String[] headers = {"Main Key", "Step No", "Action", "Expected Results", "Actual Results", "Pass/Fail", "Defects", "Comments", "Tested Date"};
                    for (String header : headers) {
                        PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7)));
                        cell.setBackgroundColor(new java.awt.Color(255, 107, 107));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setPadding(3);
                        table.addCell(cell);
                    }
                    
                    for (TestCaseTransaction transaction : headerEntry.getValue()) {
                        table.addCell(new Phrase(transaction.getMainKey() != null ? transaction.getMainKey() : ""));
                        table.addCell(new Phrase(transaction.getStepNo() != null ? transaction.getStepNo() : ""));
                        table.addCell(new Phrase(transaction.getAction() != null ? transaction.getAction() : ""));
                        table.addCell(new Phrase(transaction.getExpectedResults() != null ? transaction.getExpectedResults() : ""));
                        table.addCell(new Phrase(transaction.getActualResults() != null ? transaction.getActualResults() : ""));
                        table.addCell(new Phrase(transaction.getPassFail() != null ? transaction.getPassFail() : ""));
                        table.addCell(new Phrase(transaction.getDefects() != null ? transaction.getDefects() : ""));
                        table.addCell(new Phrase(transaction.getComments() != null ? transaction.getComments() : ""));
                        table.addCell(new Phrase(transaction.getTestedDate() != null ? transaction.getTestedDate().toString() : ""));
                    }
                    
                    document.add(table);
                    document.add(new Paragraph(" ")); // Spacing
                }
            }
        }
        
        document.close();
        return outputStream.toByteArray();
    }


    // PDF Export for Scenario Activities grouped by Business Scenarios
    public byte[] exportScenarioActivitiesToPDF(Map<String, List<ScenarioActivities>> scenariosWithActivities, String companyCode) throws DocumentException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);

        document.open();

        // Title
        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Scenario Activities Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        // Company Code and Generation Info
        com.lowagie.text.Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph info = new Paragraph("Company Code: " + companyCode + " | Generated: " +
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), infoFont);
        info.setSpacingAfter(20);
        document.add(info);

        // Summary
        int totalScenarios = scenariosWithActivities.size();
        int totalActivities = scenariosWithActivities.values().stream().mapToInt(List::size).sum();

        Paragraph summary = new Paragraph("Summary: " + totalScenarios + " Business Scenarios, " + totalActivities + " Activities",
            FontFactory.getFont(FontFactory.HELVETICA, 12));
        summary.setSpacingAfter(20);
        document.add(summary);

        // Content
        for (Map.Entry<String, List<ScenarioActivities>> entry : scenariosWithActivities.entrySet()) {
            String scenarioKey = entry.getKey();
            List<ScenarioActivities> activities = entry.getValue();

            // Business Scenario Header
            Paragraph scenarioHeader = new Paragraph("Business Scenario: " + scenarioKey,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            scenarioHeader.setSpacingBefore(10);
            scenarioHeader.setSpacingAfter(10);
            document.add(scenarioHeader);

            if (activities.isEmpty()) {
                Paragraph noActivities = new Paragraph("No activities found for this scenario.",
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
                noActivities.setSpacingAfter(10);
                document.add(noActivities);
                continue;
            }

            // Activities Table
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 3f, 3f, 2f, 1.5f});

            // Header
            String[] headers = {"Transaction Key", "Activity", "Expected Outcome", "Responsible", "T-Code"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                cell.setBackgroundColor(new java.awt.Color(33, 150, 243));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Data
            for (ScenarioActivities activity : activities) {
                table.addCell(new Phrase(activity.getTransactionKey() != null ? activity.getTransactionKey() : ""));
                table.addCell(new Phrase(activity.getActivity() != null ? activity.getActivity() : ""));
                table.addCell(new Phrase(activity.getExpectedOutcome() != null ? activity.getExpectedOutcome() : ""));
                table.addCell(new Phrase(activity.getResponsible() != null ? activity.getResponsible() : ""));
                table.addCell(new Phrase(activity.getTcode() != null ? activity.getTcode() : ""));
            }

            document.add(table);
            document.add(new Paragraph(" ")); // Spacing
        }

        document.close();
        return outputStream.toByteArray();
    }

    // Excel Export for Scenario Activities
    public byte[] exportScenarioActivitiesToExcel(Map<String, List<ScenarioActivities>> scenariosWithActivities, String companyCode) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Scenario Activities");
        
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        CellStyle scenarioStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font scenarioFont = workbook.createFont();
        scenarioFont.setBold(true);
        scenarioFont.setFontHeightInPoints((short) 14);
        scenarioStyle.setFont(scenarioFont);
        
        int rowNum = 0;
        
        for (Map.Entry<String, List<ScenarioActivities>> entry : scenariosWithActivities.entrySet()) {
            String scenarioKey = entry.getKey();
            List<ScenarioActivities> activities = entry.getValue();
            
            // Business Scenario Header
            org.apache.poi.ss.usermodel.Row scenarioRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell scenarioCell = scenarioRow.createCell(0);
            scenarioCell.setCellValue("Business Scenario: " + scenarioKey);
            scenarioCell.setCellStyle(scenarioStyle);
            
            // Table headers
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Transaction Key", "Activity", "Expected Outcome", "Responsible", "T-Code"};
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data rows
            for (ScenarioActivities activity : activities) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(activity.getTransactionKey() != null ? activity.getTransactionKey() : "");
                row.createCell(1).setCellValue(activity.getActivity() != null ? activity.getActivity() : "");
                row.createCell(2).setCellValue(activity.getExpectedOutcome() != null ? activity.getExpectedOutcome() : "");
                row.createCell(3).setCellValue(activity.getResponsible() != null ? activity.getResponsible() : "");
                row.createCell(4).setCellValue(activity.getTcode() != null ? activity.getTcode() : "");
            }
            
            rowNum++; // Empty row between scenarios
        }
        
        // Auto-size columns
        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    // Excel Export for Billable Tasks
    public byte[] exportBillableTasksToExcel(List<TicketTask> tasks, String companyCode, String startDate, String endDate, String performedBy) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Billable Tasks");
        
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        // Report Title
        org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(0);
        org.apache.poi.ss.usermodel.Cell titleCell = titleRow.createCell(0);
        String titleText = companyCode + " -  Task Report From " + startDate + " to " + endDate;
        if (performedBy != null && !performedBy.trim().isEmpty()) {
            titleText += " | Performed By: " + performedBy;
        }
        titleCell.setCellValue(titleText);
        CellStyle titleStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        
        // Header Row
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(2);
        String[] headers = {"Ticket ID", "Ticket Name / Issue Name", "Start Date", "Completed Date", "Project", "Requestor Task", "Performed By", "Last Modified By", "Billable Hours", "Status"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data Rows
        int rowNum = 3;
        double totalHours = 0;
        java.util.Map<String, Double> hoursPerPerson = new java.util.HashMap<>();
        
        for (TicketTask task : tasks) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            String ticketId = (task.getTicket() != null && task.getTicket().getTicketNo() != null) ? task.getTicket().getTicketNo() : (task.getTicket() != null ? String.valueOf(task.getTicket().getId()) : "");
            row.createCell(0).setCellValue(ticketId);
            
            String ticketName = (task.getTicket() != null) ? (task.getTicket().getIssueDescription() != null ? task.getTicket().getIssueDescription() : (task.getTicket().getSummary() != null ? task.getTicket().getSummary() : (task.getTicket().getDescription() != null ? task.getTicket().getDescription() : "-"))) : "-";
            row.createCell(1).setCellValue(ticketName);
            
            row.createCell(2).setCellValue(task.getStartDate() != null ? task.getStartDate().toString() : "");
            row.createCell(3).setCellValue(task.getCompletedDate() != null ? task.getCompletedDate().toString() : "");
            
            String project = task.getTicket() != null ? (task.getTicket().getApplicationCode() != null ? task.getTicket().getApplicationCode() : task.getTicket().getCompanyCode()) : "";
            row.createCell(4).setCellValue(project);
            
            row.createCell(5).setCellValue(task.getRequestorTask() != null ? task.getRequestorTask() : "");
            
            String person = task.getPerformedBy() != null ? task.getPerformedBy() : "Unknown";
            row.createCell(6).setCellValue(person);
            
            String lastMod = task.getLastModifiedBy();
            if (lastMod == null || lastMod.trim().isEmpty()) {
                lastMod = task.getPerformedBy();
            }
            row.createCell(7).setCellValue(lastMod != null ? lastMod : "-");
            
            double hours = task.getBillableHours() != null ? task.getBillableHours() : 0.0;
            row.createCell(8).setCellValue(hours);
            totalHours += hours;
            hoursPerPerson.put(person, hoursPerPerson.getOrDefault(person, 0.0) + hours);
            
            row.createCell(9).setCellValue(task.getStatus() != null ? task.getStatus() : "");
        }
        
        // Blank row
        rowNum++;
        
        // Summary Header
        org.apache.poi.ss.usermodel.Row summaryHeaderRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell summaryLabelCell = summaryHeaderRow.createCell(7);
        summaryLabelCell.setCellValue("User Summary");
        CellStyle totalStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font totalFont = workbook.createFont();
        totalFont.setBold(true);
        totalStyle.setFont(totalFont);
        summaryLabelCell.setCellStyle(totalStyle);
        
        // Summary Rows
        for (java.util.Map.Entry<String, Double> entry : hoursPerPerson.entrySet()) {
            org.apache.poi.ss.usermodel.Row personRow = sheet.createRow(rowNum++);
            org.apache.poi.ss.usermodel.Cell personNameCell = personRow.createCell(7);
            personNameCell.setCellValue(entry.getKey() + " :");
            personNameCell.setCellStyle(totalStyle);
            
            org.apache.poi.ss.usermodel.Cell personHoursCell = personRow.createCell(8);
            personHoursCell.setCellValue(entry.getValue());
            personHoursCell.setCellStyle(totalStyle);
        }
        
        // Total Row
        org.apache.poi.ss.usermodel.Row totalRow = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell totalLabelCell = totalRow.createCell(7);
        totalLabelCell.setCellValue("Overall Total:");
        totalLabelCell.setCellStyle(totalStyle);
        
        org.apache.poi.ss.usermodel.Cell totalValueCell = totalRow.createCell(8);
        totalValueCell.setCellValue(totalHours);
        totalValueCell.setCellStyle(totalStyle);
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    // PDF Export for Billable Tasks
    public byte[] exportBillableTasksToPDF(List<TicketTask> tasks, String companyCode, String startDate, String endDate, String performedBy) throws DocumentException {
        if (tasks == null) {
            tasks = new java.util.ArrayList<>();
        }

        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);

        document.open();

        // Borderless top header table
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        try {
            headerTable.setWidths(new float[]{1f, 3f});
        } catch (Exception e) {
            // fallback
        }
        headerTable.setSpacingAfter(20);

        // Left cell: Company code/logo
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(PdfPCell.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph companyPara = new Paragraph(companyCode, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new java.awt.Color(30, 41, 59)));
        logoCell.addElement(companyPara);
        headerTable.addCell(logoCell);

        // Right cell: Report Title, Date Range & Total Badge
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(PdfPCell.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        
        Paragraph titlePara = new Paragraph("Task Report  |  Total Tasks: " + tasks.size(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new java.awt.Color(15, 23, 42)));
        titlePara.setAlignment(Element.ALIGN_RIGHT);
        titleCell.addElement(titlePara);

        String dateRangeStr = "From " + startDate + " to " + endDate;
        if (performedBy != null && !performedBy.trim().isEmpty()) {
            dateRangeStr += "  |  User: " + performedBy;
        }
        Paragraph subtitlePara = new Paragraph(dateRangeStr + "   (Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ")", FontFactory.getFont(FontFactory.HELVETICA, 9, new java.awt.Color(100, 116, 139)));
        subtitlePara.setAlignment(Element.ALIGN_RIGHT);
        subtitlePara.setSpacingBefore(4);
        titleCell.addElement(subtitlePara);

        headerTable.addCell(titleCell);
        document.add(headerTable);

        // Table
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        try {
            table.setWidths(new float[]{1.2f, 2.2f, 1.2f, 1.2f, 1.2f, 1.8f, 1.8f, 1.8f, 1.2f, 1.0f});
        } catch (Exception e) {
            // fallback
        }

        // Header
        String[] headers = {"Ticket ID", "Ticket Name / Issue Name", "Start Date", "Completed Date", "Project", "Requestor Task", "Performed By", "Last Modified By", "Billable Hours", "Status"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header.toUpperCase(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new java.awt.Color(30, 41, 59))));
            cell.setBackgroundColor(new java.awt.Color(241, 245, 249));
            cell.setBorder(PdfPCell.BOTTOM);
            cell.setBorderWidthBottom(2f);
            cell.setBorderColorBottom(new java.awt.Color(15, 23, 42));
            
            if ("Billable Hours".equalsIgnoreCase(header)) {
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            } else {
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            }
            cell.setPaddingTop(8f);
            cell.setPaddingBottom(8f);
            cell.setPaddingLeft(6f);
            cell.setPaddingRight(6f);
            table.addCell(cell);
        }

        // Data
        double totalHours = 0;
        java.util.Map<String, Double> hoursPerPerson = new java.util.HashMap<>();
        
        for (TicketTask task : tasks) {
            String ticketId = (task.getTicket() != null && task.getTicket().getTicketNo() != null) ? task.getTicket().getTicketNo() : (task.getTicket() != null ? String.valueOf(task.getTicket().getId()) : "");
            PdfPCell cellId = new PdfPCell(new Phrase(ticketId, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new java.awt.Color(37, 99, 235))));
            cellId.setBorder(PdfPCell.BOTTOM);
            cellId.setBorderColor(new java.awt.Color(226, 232, 240));
            cellId.setPadding(6f);
            table.addCell(cellId);
            
            String ticketName = (task.getTicket() != null) ? (task.getTicket().getIssueDescription() != null ? task.getTicket().getIssueDescription() : (task.getTicket().getSummary() != null ? task.getTicket().getSummary() : (task.getTicket().getDescription() != null ? task.getTicket().getDescription() : "-"))) : "-";
            PdfPCell cellName = new PdfPCell(new Phrase(ticketName, FontFactory.getFont(FontFactory.HELVETICA, 8, new java.awt.Color(51, 65, 85))));
            cellName.setBorder(PdfPCell.BOTTOM);
            cellName.setBorderColor(new java.awt.Color(226, 232, 240));
            cellName.setPadding(6f);
            table.addCell(cellName);
            
            PdfPCell cellStart = new PdfPCell(new Phrase(task.getStartDate() != null ? task.getStartDate().toString() : "", FontFactory.getFont(FontFactory.HELVETICA, 8, new java.awt.Color(100, 116, 139))));
            cellStart.setBorder(PdfPCell.BOTTOM);
            cellStart.setBorderColor(new java.awt.Color(226, 232, 240));
            cellStart.setPadding(6f);
            table.addCell(cellStart);
            
            PdfPCell cellEnd = new PdfPCell(new Phrase(task.getCompletedDate() != null ? task.getCompletedDate().toString() : "", FontFactory.getFont(FontFactory.HELVETICA, 8, new java.awt.Color(100, 116, 139))));
            cellEnd.setBorder(PdfPCell.BOTTOM);
            cellEnd.setBorderColor(new java.awt.Color(226, 232, 240));
            cellEnd.setPadding(6f);
            table.addCell(cellEnd);
            
            String project = task.getTicket() != null ? (task.getTicket().getApplicationCode() != null ? task.getTicket().getApplicationCode() : task.getTicket().getCompanyCode()) : "";
            PdfPCell cellProj = new PdfPCell(new Phrase(project, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new java.awt.Color(51, 65, 85))));
            cellProj.setBorder(PdfPCell.BOTTOM);
            cellProj.setBorderColor(new java.awt.Color(226, 232, 240));
            cellProj.setPadding(6f);
            table.addCell(cellProj);
            
            PdfPCell cellReq = new PdfPCell(new Phrase(task.getRequestorTask() != null ? task.getRequestorTask() : "", FontFactory.getFont(FontFactory.HELVETICA, 8, new java.awt.Color(51, 65, 85))));
            cellReq.setBorder(PdfPCell.BOTTOM);
            cellReq.setBorderColor(new java.awt.Color(226, 232, 240));
            cellReq.setPadding(6f);
            table.addCell(cellReq);
            
            String person = task.getPerformedBy() != null ? task.getPerformedBy() : "Unknown";
            PdfPCell cellPerson = new PdfPCell(new Phrase(person, FontFactory.getFont(FontFactory.HELVETICA, 8, new java.awt.Color(51, 65, 85))));
            cellPerson.setBorder(PdfPCell.BOTTOM);
            cellPerson.setBorderColor(new java.awt.Color(226, 232, 240));
            cellPerson.setPadding(6f);
            table.addCell(cellPerson);
            
            String lastMod = task.getLastModifiedBy();
            if (lastMod == null || lastMod.trim().isEmpty()) {
                lastMod = task.getPerformedBy();
            }
            PdfPCell cellMod = new PdfPCell(new Phrase(lastMod != null ? lastMod : "-", FontFactory.getFont(FontFactory.HELVETICA, 8, new java.awt.Color(51, 65, 85))));
            cellMod.setBorder(PdfPCell.BOTTOM);
            cellMod.setBorderColor(new java.awt.Color(226, 232, 240));
            cellMod.setPadding(6f);
            table.addCell(cellMod);
            
            double hours = task.getBillableHours() != null ? task.getBillableHours() : 0.0;
            PdfPCell cellHours = new PdfPCell(new Phrase(String.format("%.1f", hours), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new java.awt.Color(30, 41, 59))));
            cellHours.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellHours.setBorder(PdfPCell.BOTTOM);
            cellHours.setBorderColor(new java.awt.Color(226, 232, 240));
            cellHours.setPadding(6f);
            table.addCell(cellHours);
            
            totalHours += hours;
            hoursPerPerson.put(person, hoursPerPerson.getOrDefault(person, 0.0) + hours);
            
            String statusStr = task.getStatus() != null ? task.getStatus() : "";
            PdfPCell cellStatus = new PdfPCell(new Phrase(statusStr, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, 
                "CLOSED".equalsIgnoreCase(statusStr) ? new java.awt.Color(22, 163, 74) : 
                ("IN_PROGRESS".equalsIgnoreCase(statusStr) ? new java.awt.Color(217, 119, 6) : new java.awt.Color(37, 99, 235))
            )));
            cellStatus.setBorder(PdfPCell.BOTTOM);
            cellStatus.setBorderColor(new java.awt.Color(226, 232, 240));
            cellStatus.setPadding(6f);
            table.addCell(cellStatus);
        }

        document.add(table);

        // Add spacing after data table
        document.add(new Paragraph(" "));

        // Build summary table
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(35);
        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryTable.setSpacingBefore(15);
        try {
            summaryTable.setWidths(new float[]{2f, 1f});
        } catch (Exception e) {
            // fallback
        }

        // Summary Header
        PdfPCell sHeader1 = new PdfPCell(new Phrase("PERFORMED BY", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new java.awt.Color(30, 41, 59))));
        sHeader1.setBackgroundColor(new java.awt.Color(241, 245, 249));
        sHeader1.setBorder(PdfPCell.BOTTOM);
        sHeader1.setBorderWidthBottom(1.5f);
        sHeader1.setBorderColorBottom(new java.awt.Color(15, 23, 42));
        sHeader1.setPadding(6f);
        summaryTable.addCell(sHeader1);

        PdfPCell sHeader2 = new PdfPCell(new Phrase("HOURS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new java.awt.Color(30, 41, 59))));
        sHeader2.setBackgroundColor(new java.awt.Color(241, 245, 249));
        sHeader2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        sHeader2.setBorder(PdfPCell.BOTTOM);
        sHeader2.setBorderWidthBottom(1.5f);
        sHeader2.setBorderColorBottom(new java.awt.Color(15, 23, 42));
        sHeader2.setPadding(6f);
        summaryTable.addCell(sHeader2);

        // Summary Rows
        for (java.util.Map.Entry<String, Double> entry : hoursPerPerson.entrySet()) {
            PdfPCell cell1 = new PdfPCell(new Phrase(entry.getKey(), FontFactory.getFont(FontFactory.HELVETICA, 8, new java.awt.Color(51, 65, 85))));
            cell1.setBorder(PdfPCell.BOTTOM);
            cell1.setBorderColor(new java.awt.Color(241, 245, 249));
            cell1.setPadding(6f);
            summaryTable.addCell(cell1);

            PdfPCell cell2 = new PdfPCell(new Phrase(String.format("%.1f", entry.getValue()), FontFactory.getFont(FontFactory.HELVETICA, 8, new java.awt.Color(30, 41, 59))));
            cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell2.setBorder(PdfPCell.BOTTOM);
            cell2.setBorderColor(new java.awt.Color(241, 245, 249));
            cell2.setPadding(6f);
            summaryTable.addCell(cell2);
        }

        // Overall Total Row
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("OVERALL TOTAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new java.awt.Color(15, 23, 42))));
        totalLabelCell.setBorder(PdfPCell.BOTTOM);
        totalLabelCell.setBorderWidthBottom(1.5f);
        totalLabelCell.setBorderColorBottom(new java.awt.Color(15, 23, 42));
        totalLabelCell.setPadding(8f);
        summaryTable.addCell(totalLabelCell);

        PdfPCell totalValueCell = new PdfPCell(new Phrase(String.format("%.1f", totalHours), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new java.awt.Color(22, 163, 74))));
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setBorder(PdfPCell.BOTTOM);
        totalValueCell.setBorderWidthBottom(1.5f);
        totalValueCell.setBorderColorBottom(new java.awt.Color(15, 23, 42));
        totalValueCell.setPadding(8f);
        summaryTable.addCell(totalValueCell);

        document.add(summaryTable);
        document.close();

        return outputStream.toByteArray();
    }

    // Individual Ticket PDF Export
    public byte[] exportTicketToPDF(com.example.demo.entity.Ticket ticket, List<com.example.demo.entity.TicketComment> comments, List<com.example.demo.entity.TicketTask> tasks, String logoFilename) throws DocumentException {
        if (tasks == null) {
            tasks = new java.util.ArrayList<>();
        }

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);

        document.open();

        // Add Logo
        if (logoFilename != null && !logoFilename.isEmpty()) {
            try {
                com.lowagie.text.Image logo = null;
                if (companyRefRepository != null) {
                    String companyCode = logoFilename.contains("_") ? logoFilename.split("_")[0] : logoFilename;
                    java.util.Optional<com.example.demo.entity.CompanyRef> companyOpt = companyRefRepository.findById(companyCode);
                    if (companyOpt.isPresent() && companyOpt.get().getLogoData() != null && companyOpt.get().getLogoData().length > 0) {
                        logo = com.lowagie.text.Image.getInstance(companyOpt.get().getLogoData());
                    }
                }
                if (logo == null) {
                    java.nio.file.Path logoPath = java.nio.file.Paths.get("uploads", "logos", logoFilename);
                    if (java.nio.file.Files.exists(logoPath)) {
                        logo = com.lowagie.text.Image.getInstance(logoPath.toAbsolutePath().toString());
                    }
                }
                if (logo != null) {
                    logo.scaleToFit(120, 60);
                    logo.setAlignment(Element.ALIGN_LEFT);
                    document.add(logo);
                    document.add(new Paragraph(" "));
                }
            } catch (Exception e) {
                System.err.println("Could not load logo for PDF: " + e.getMessage());
            }
        }

        // Styles
        com.lowagie.text.Font mainTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new java.awt.Color(15, 52, 96));
        com.lowagie.text.Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new java.awt.Color(233, 69, 96));
        com.lowagie.text.Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new java.awt.Color(102, 102, 102));
        com.lowagie.text.Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new java.awt.Color(51, 51, 51));
        com.lowagie.text.Font commentFont = FontFactory.getFont(FontFactory.HELVETICA, 10, new java.awt.Color(33, 33, 33));

        // Header Title
        Paragraph title = new Paragraph("Ticket Details: " + ticket.getTicketNo(), mainTitleFont);
        title.setAlignment(Element.ALIGN_LEFT);
        title.setSpacingAfter(20);
        document.add(title);

        // Basic Info Table
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(20);

        addPdfRow(infoTable, "Status:", ticket.getStatus(), labelFont, valueFont);
        addPdfRow(infoTable, "Priority:", ticket.getPriority(), labelFont, valueFont);
        addPdfRow(infoTable, "Issue Type:", ticket.getIssueType(), labelFont, valueFont);
        addPdfRow(infoTable, "Application:", ticket.getApplicationCode(), labelFont, valueFont);
        addPdfRow(infoTable, "Module:", ticket.getModuleCode(), labelFont, valueFont);
        addPdfRow(infoTable, "Test Case:", ticket.getTestCaseCode(), labelFont, valueFont);
        addPdfRow(infoTable, "Transaction Key:", ticket.getTransactionMainKey(), labelFont, valueFont);
        addPdfRow(infoTable, "Created By:", ticket.getUserCode(), labelFont, valueFont);
        addPdfRow(infoTable, "Created Date:", ticket.getCreatedDate() != null ? ticket.getCreatedDate().toString() : "-", labelFont, valueFont);

        document.add(infoTable);

        // Summary & Description
        Paragraph summaryTitle = new Paragraph("Summary", sectionTitleFont);
        summaryTitle.setSpacingAfter(5);
        document.add(summaryTitle);
        document.add(new Paragraph(ticket.getIssueDescription() != null ? ticket.getIssueDescription() : ticket.getSummary(), valueFont));
        document.add(new Paragraph(" "));

        Paragraph descTitle = new Paragraph("Detailed Description", sectionTitleFont);
        descTitle.setSpacingAfter(5);
        document.add(descTitle);
        document.add(new Paragraph(ticket.getIssueLongDescription() != null ? ticket.getIssueLongDescription() : (ticket.getDescription() != null ? ticket.getDescription() : "No description provided"), valueFont));
        document.add(new Paragraph(" "));

        // Steps to Reproduce
        if (ticket.getStepsToReproduce() != null && !ticket.getStepsToReproduce().isEmpty()) {
            Paragraph stepsTitle = new Paragraph("Steps to Reproduce", sectionTitleFont);
            stepsTitle.setSpacingAfter(5);
            document.add(stepsTitle);
            document.add(new Paragraph(ticket.getStepsToReproduce(), valueFont));
            document.add(new Paragraph(" "));
        }

        // Expected & Actual Results
        if ((ticket.getExpectedResult() != null && !ticket.getExpectedResult().isEmpty()) || 
            (ticket.getActualResult() != null && !ticket.getActualResult().isEmpty())) {
            
            PdfPTable resultTable = new PdfPTable(2);
            resultTable.setWidthPercentage(100);
            resultTable.setSpacingAfter(10);

            if (ticket.getExpectedResult() != null && !ticket.getExpectedResult().isEmpty()) {
                PdfPCell cell = new PdfPCell(new Paragraph("Expected Result", sectionTitleFont));
                cell.setBorder(0);
                resultTable.addCell(cell);
            } else {
                resultTable.addCell(new PdfPCell(new Paragraph(" ")));
            }

            if (ticket.getActualResult() != null && !ticket.getActualResult().isEmpty()) {
                PdfPCell cell = new PdfPCell(new Paragraph("Actual Result", sectionTitleFont));
                cell.setBorder(0);
                resultTable.addCell(cell);
            } else {
                resultTable.addCell(new PdfPCell(new Paragraph(" ")));
            }

            resultTable.addCell(new PdfPCell(new Paragraph(ticket.getExpectedResult() != null ? ticket.getExpectedResult() : "", valueFont)));
            resultTable.addCell(new PdfPCell(new Paragraph(ticket.getActualResult() != null ? ticket.getActualResult() : "", valueFont)));

            document.add(resultTable);
            document.add(new Paragraph(" "));
        }

        // Assignees Section
        if (ticket.getAssigneeUserCode() != null || ticket.getAssignee2UserCode() != null) {
            Paragraph assigneeTitle = new Paragraph("Assignees", sectionTitleFont);
            assigneeTitle.setSpacingAfter(10);
            document.add(assigneeTitle);

            PdfPTable assigneeTable = new PdfPTable(3);
            assigneeTable.setWidthPercentage(100);
            assigneeTable.addCell(new PdfPCell(new Phrase("Level", labelFont)));
            assigneeTable.addCell(new PdfPCell(new Phrase("User", labelFont)));
            assigneeTable.addCell(new PdfPCell(new Phrase("Expected Date", labelFont)));

            if (ticket.getAssigneeUserCode() != null) {
                assigneeTable.addCell(new Phrase("Primary", valueFont));
                assigneeTable.addCell(new Phrase(ticket.getAssigneeName() != null ? ticket.getAssigneeName() : ticket.getAssigneeUserCode(), valueFont));
                assigneeTable.addCell(new Phrase(ticket.getAssigneeExpectedDate1() != null ? ticket.getAssigneeExpectedDate1().toString() : "-", valueFont));
            }
            if (ticket.getAssignee2UserCode() != null) {
                assigneeTable.addCell(new Phrase("Secondary", valueFont));
                assigneeTable.addCell(new Phrase(ticket.getAssignee2UserCode(), valueFont));
                assigneeTable.addCell(new Phrase(ticket.getAssignee2ExpectedDate() != null ? ticket.getAssignee2ExpectedDate().toString() : "-", valueFont));
            }
            document.add(assigneeTable);
            document.add(new Paragraph(" "));
        }

        // Tasks Section
        if (tasks != null && !tasks.isEmpty()) {
            Paragraph tasksTitle = new Paragraph("Billable Tasks", sectionTitleFont);
            tasksTitle.setSpacingAfter(10);
            document.add(tasksTitle);

            PdfPTable tasksTable = new PdfPTable(4);
            tasksTable.setWidthPercentage(100);
            String[] taskHeaders = {"Task Type", "Performed By", "Hours", "Date"};
            for (String h : taskHeaders) {
                PdfPCell cell = new PdfPCell(new Phrase(h, labelFont));
                cell.setBackgroundColor(new java.awt.Color(240, 240, 240));
                tasksTable.addCell(cell);
            }

            for (com.example.demo.entity.TicketTask task : tasks) {
                tasksTable.addCell(new Phrase(task.getTaskType(), valueFont));
                tasksTable.addCell(new Phrase(task.getPerformedBy(), valueFont));
                tasksTable.addCell(new Phrase(String.valueOf(task.getBillableHours()), valueFont));
                tasksTable.addCell(new Phrase(task.getCompletedDate() != null ? task.getCompletedDate().toString() : "-", valueFont));
            }
            document.add(tasksTable);
            document.add(new Paragraph(" "));
        }

        // Comments Section
        if (comments != null && !comments.isEmpty()) {
            Paragraph commentsTitle = new Paragraph("Comments", sectionTitleFont);
            commentsTitle.setSpacingAfter(10);
            document.add(commentsTitle);

            for (com.example.demo.entity.TicketComment comment : comments) {
                Paragraph cHeader = new Paragraph(comment.getCommentedBy() + " on " + 
                    (comment.getCommentedAt() != null ? comment.getCommentedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "-"), labelFont);
                document.add(cHeader);
                Paragraph cBody = new Paragraph(comment.getComment(), commentFont);
                cBody.setIndentationLeft(10);
                cBody.setSpacingAfter(10);
                document.add(cBody);
            }
        }

        // Resolution Section
        if ((ticket.getAssignee1Resolution() != null && !ticket.getAssignee1Resolution().isEmpty()) || 
            (ticket.getAssignee2Resolution() != null && !ticket.getAssignee2Resolution().isEmpty()) || 
            (ticket.getClosingNotes() != null && !ticket.getClosingNotes().isEmpty())) {
            
            Paragraph resTitle = new Paragraph("Resolution Details", sectionTitleFont);
            resTitle.setSpacingAfter(10);
            document.add(resTitle);

            if (ticket.getAssignee1Resolution() != null && !ticket.getAssignee1Resolution().isEmpty()) {
                document.add(new Paragraph("Primary Resolution: " + ticket.getAssignee1Resolution(), valueFont));
            }
            if (ticket.getAssignee2Resolution() != null && !ticket.getAssignee2Resolution().isEmpty()) {
                document.add(new Paragraph("Secondary Resolution: " + ticket.getAssignee2Resolution(), valueFont));
            }
            if (ticket.getClosingNotes() != null && !ticket.getClosingNotes().isEmpty()) {
                document.add(new Paragraph("Closing Notes: " + ticket.getClosingNotes(), valueFont));
            }
            document.add(new Paragraph(" "));
        }

        // Footer
        Paragraph footer = new Paragraph("\u00a9 " + java.time.Year.now().getValue() + " PERFECTQA - Quality Assured with Precision", labelFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30);
        document.add(footer);

        document.close();
        return outputStream.toByteArray();
    }

    private void addPdfRow(PdfPTable table, String label, String value, com.lowagie.text.Font labelFont, com.lowagie.text.Font valueFont) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, labelFont));
        lCell.setBorder(0);
        lCell.setPadding(5);
        table.addCell(lCell);

        PdfPCell vCell = new PdfPCell(new Phrase(value != null ? value : "-", valueFont));
        vCell.setBorder(0);
        vCell.setPadding(5);
        table.addCell(vCell);
    }

    // Excel Export for Tickets
    public byte[] exportTicketsToExcel(List<com.example.demo.entity.Ticket> tickets, String companyCode) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("IT Tickets");
        
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        String[] headers = {"Ticket ID", "Issue", "Issue Description", "Created By", "Assignee Name", "Status"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        int rowNum = 1;
        for (com.example.demo.entity.Ticket ticket : tickets) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(ticket.getTicketNo() != null ? ticket.getTicketNo() : String.valueOf(ticket.getId()));
            String issueVal = (ticket.getIssueCode() != null && !ticket.getIssueCode().trim().isEmpty()) ? ticket.getIssueCode() : (ticket.getIssueType() != null ? ticket.getIssueType() : "");
            row.createCell(1).setCellValue(issueVal);
            
            String descVal = (ticket.getIssueDescription() != null && !ticket.getIssueDescription().trim().isEmpty()) ? ticket.getIssueDescription() : (ticket.getSummary() != null ? ticket.getSummary() : "");
            row.createCell(2).setCellValue(descVal);
            
            // Resolve creator's name
            String creatorName = ticket.getUserCode() != null ? ticket.getUserCode() : (ticket.getCreatedBy() != null ? ticket.getCreatedBy() : "");
            if (creatorName != null && !creatorName.trim().isEmpty() && userAccountService != null) {
                com.example.demo.entity.UserAccount user = userAccountService.findByUserId(creatorName.trim());
                if (user != null) {
                    String fullName = "";
                    if (user.getFirstName() != null) fullName += user.getFirstName();
                    if (user.getLastName() != null) {
                        if (!fullName.isEmpty()) fullName += " ";
                        fullName += user.getLastName();
                    }
                    if (!fullName.isEmpty()) {
                        creatorName = fullName;
                    }
                }
            }
            row.createCell(3).setCellValue(creatorName);
            row.createCell(4).setCellValue(ticket.getAssigneeName() != null ? ticket.getAssigneeName() : (ticket.getAssignedTo() != null ? ticket.getAssignedTo() : ""));
            row.createCell(5).setCellValue(ticket.getStatus() != null ? ticket.getStatus() : "");
        }
        
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    // PDF Export for Tickets
    public byte[] exportTicketsToPDF(List<com.example.demo.entity.Ticket> tickets, String companyCode, String reportTitle) throws DocumentException {
        Document document = new Document(PageSize.A4.rotate());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        
        document.open();
        
        // Title
        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph(reportTitle != null ? reportTitle : "Tickets Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);
        
        // Company Code & Info
        com.lowagie.text.Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Paragraph info = new Paragraph("Company Code: " + companyCode + " | Total Tickets: " + (tickets != null ? tickets.size() : 0) + " | Generated: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), infoFont);
        info.setSpacingAfter(15);
        document.add(info);
        
        // Table
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 2f, 4f, 2f, 2f, 1.5f});
        
        // Header
        String[] headers = {"Ticket ID", "Issue", "Issue Description", "Created By", "Assignee Name", "Status"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.WHITE)));
            cell.setBackgroundColor(new java.awt.Color(30, 58, 138));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }
        
        // Data
        com.lowagie.text.Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        if (tickets != null) {
            for (com.example.demo.entity.Ticket ticket : tickets) {
                String idVal = ticket.getTicketNo() != null ? ticket.getTicketNo() : String.valueOf(ticket.getId());
                PdfPCell idCell = new PdfPCell(new Phrase(idVal != null ? idVal : "", dataFont));
                idCell.setPadding(5);
                table.addCell(idCell);
                
                String issueVal = (ticket.getIssueCode() != null && !ticket.getIssueCode().trim().isEmpty()) ? ticket.getIssueCode() : (ticket.getIssueType() != null ? ticket.getIssueType() : "");
                PdfPCell issueCell = new PdfPCell(new Phrase(issueVal != null ? issueVal : "", dataFont));
                issueCell.setPadding(5);
                table.addCell(issueCell);
                
                String descVal = (ticket.getIssueDescription() != null && !ticket.getIssueDescription().trim().isEmpty()) ? ticket.getIssueDescription() : (ticket.getSummary() != null ? ticket.getSummary() : "");
                PdfPCell descCell = new PdfPCell(new Phrase(descVal != null ? descVal : "", dataFont));
                descCell.setPadding(5);
                table.addCell(descCell);
                
                // Resolve creator's name
                String creatorName = ticket.getUserCode() != null ? ticket.getUserCode() : (ticket.getCreatedBy() != null ? ticket.getCreatedBy() : "");
                if (creatorName != null && !creatorName.trim().isEmpty() && userAccountService != null) {
                    com.example.demo.entity.UserAccount user = userAccountService.findByUserId(creatorName.trim());
                    if (user != null) {
                        String fullName = "";
                        if (user.getFirstName() != null) fullName += user.getFirstName();
                        if (user.getLastName() != null) {
                            if (!fullName.isEmpty()) fullName += " ";
                            fullName += user.getLastName();
                        }
                        if (!fullName.isEmpty()) {
                            creatorName = fullName;
                        }
                    }
                }
                PdfPCell creatorCell = new PdfPCell(new Phrase(creatorName != null ? creatorName : "", dataFont));
                creatorCell.setPadding(5);
                table.addCell(creatorCell);
                
                String assigneeVal = ticket.getAssigneeName() != null ? ticket.getAssigneeName() : (ticket.getAssignedTo() != null ? ticket.getAssignedTo() : "");
                PdfPCell assigneeCell = new PdfPCell(new Phrase(assigneeVal != null ? assigneeVal : "", dataFont));
                assigneeCell.setPadding(5);
                table.addCell(assigneeCell);
                
                String statusVal = ticket.getStatus() != null ? ticket.getStatus() : "";
                PdfPCell statusCell = new PdfPCell(new Phrase(statusVal != null ? statusVal : "", dataFont));
                statusCell.setPadding(5);
                table.addCell(statusCell);
            }
        }
        
        document.add(table);
        document.close();
        
        return outputStream.toByteArray();
    }

    private boolean isEmailPresentForTask(TicketTask task) {
        if (task == null) {
            return false;
        }
        String person = task.getPerformedBy();
        if (person == null || person.trim().isEmpty()) {
            return false;
        }
        
        // If the performedBy username itself contains '@', consider email is present
        if (person.contains("@")) {
            return true;
        }
        
        // Otherwise look up the user account
        if (userAccountService != null) {
            com.example.demo.entity.UserAccount user = userAccountService.findByUserId(person.trim());
            if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}

