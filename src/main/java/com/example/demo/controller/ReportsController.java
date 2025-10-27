package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.dto.ScenarioResultDTO;
import com.example.demo.repo.ReportsRepository;
import com.example.demo.service.ReportsService;


@Controller
@RequestMapping("/api")
public class ReportsController {

	
	private final ReportsService reportsService;
	@Autowired
	private ReportsRepository reportRepository;

    public ReportsController(ReportsService scenarioService) {
        this.reportsService = scenarioService;
    }

	/*
	 * @GetMapping("/reporttwoscenarios") public String showScenarios(Model model) {
	 * List<BusinessScenarioDTO> scenarios = reportsService.getAllScenarios();
	 * model.addAttribute("scenarios", scenarios); return
	 * "sceneroisandheaderreport"; }
	 */
  

    
    @GetMapping("/reporttwoscenarios")
    public String getScenariosReport(Model model) {
        List<ScenarioResultDTO> scenarios = reportsService.getReport(); // ✅ service does projection -> DTO mapping
        model.addAttribute("scenarios", scenarios);
        return "sceneroisandheaderreport";
    }
    
    @GetMapping("/detailed")
    public List<ScenarioResultDTO> getDetailedTransactions() {
        return reportsService.getDetailedTransactions();
    }
    
    @GetMapping("/detaiedtranspage")
    public String getDetailedTransactions(Model model) {
        model.addAttribute("detailed", reportsService.getDetailedTransactions());
        return "detailedtransactions"; // name of Thymeleaf file
    }
}
