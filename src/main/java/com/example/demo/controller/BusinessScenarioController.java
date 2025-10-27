package com.example.demo.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.dto.BusinessScenarioDTO;
import com.example.demo.dto.ScenariotransactionDTO;
import com.example.demo.service.BusinessScenarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api/scenario")
public class BusinessScenarioController {

    @Autowired
    private BusinessScenarioService scenarioService;

    // Show add form
    @GetMapping("/addheader")
    public String showScenarioForm(Model model) {
        model.addAttribute("header", new BusinessScenarioDTO());
        return "addheaderscenario";
    }

    @PostMapping("/savescenarioheader")
    public String saveScenario(@ModelAttribute("header") BusinessScenarioDTO dto, Model model) {
        if (scenarioService.existsByBusinessScenario(dto.getBusinessScenario())) {
            model.addAttribute("error", "Business Scenario already exists.");
            model.addAttribute("header", dto); // repopulate form with previous input
            return "addheaderscenario";
        }

        scenarioService.saveScenario(dto);
        model.addAttribute("success", true);
        model.addAttribute("successMessage", "Business Scenario saved successfully!");
        model.addAttribute("header", new BusinessScenarioDTO());
        return "addheaderscenario";
    }


    // List all scenarios
    @GetMapping("/all-scenarios")
    public String listAllScenarios(HttpSession session, Model model)  {
        List<BusinessScenarioDTO> allScenarios = scenarioService.getAllScenarios();
        model.addAttribute("scenarios", allScenarios);
        model.addAttribute("role", session.getAttribute("role"));
        return "headerscenariolist";
    }


    // Show edit form
    @GetMapping("/edit/{businessScenario}")
    public String showEditForm(@PathVariable String businessScenario, Model model) {
        BusinessScenarioDTO dto = scenarioService.getScenarioDTOById(businessScenario);
        model.addAttribute("scenario", dto);
        return "edit-scenario";
    }

    // Update scenario
    @PostMapping("/update")
    public String updateScenario(@ModelAttribute("scenario") BusinessScenarioDTO dto) {
        scenarioService.updateScenario(dto.getBusinessScenario(), dto);
        return "redirect:/api/scenario/all-scenarios";
    }
    
    @GetMapping("/addtransactions/addform")
    public String showTransactionFormForScenario(@org.springframework.web.bind.annotation.RequestParam("scenarioId") String scenarioId, Model model) {
        ScenariotransactionDTO dto = new ScenariotransactionDTO();
        dto.setBusinessScenario(scenarioId);  // pre-fill scenarioId in form

        model.addAttribute("transaction", dto);
        return "addscenariotransaction"; // Thymeleaf template
    }
    
    @GetMapping("/scenarios")
    public String viewAllScenarios(Model model) {
        List<BusinessScenarioDTO> scenarios = scenarioService.getAllScenarios();
        model.addAttribute("scenarios", scenarios);
        return "scenariotransactionlist";  // matches Thymeleaf template
    }

    @GetMapping("/all-scenariosid")
    public String listAllScenarios(Model model, @RequestParam(value = "editId", required = false) String editId) {
        List<BusinessScenarioDTO> allScenarios = scenarioService.getAllScenarios();
        model.addAttribute("scenarios", allScenarios);
        model.addAttribute("editId", editId); // pass current editing row id
        return "headerscenariolist";
    }
   


}



