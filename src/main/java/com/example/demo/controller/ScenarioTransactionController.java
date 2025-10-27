package com.example.demo.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dto.ScenariotransactionDTO;
import com.example.demo.dto.TransactionWrapperDTO;
import com.example.demo.entity.BusinessScenario;
import com.example.demo.entity.Scenariotransaction;
import com.example.demo.repo.BusinessScenarioRepository;
import com.example.demo.repo.ScenarioTransactionRepository;
import com.example.demo.service.ScenarioTransactionService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api/transactions")

public class ScenarioTransactionController {

    @Autowired
    private ScenarioTransactionService transactionService;

    @Autowired
    private BusinessScenarioRepository businessScenarioRepository;
    
    @Autowired
    private ScenarioTransactionRepository scenarioTransactionRepository;

    // ✅ Show form to add transactions for a selected business scenario
    @GetMapping("/addtransactions")
    public String showTransactionForm(@RequestParam("scenarioId") String scenarioId,HttpSession session, Model model) {
        TransactionWrapperDTO wrapper = new TransactionWrapperDTO();
        wrapper.setTransactions(Arrays.asList(new ScenariotransactionDTO(), new ScenariotransactionDTO()));
        model.addAttribute("wrapper", wrapper);

        List<BusinessScenario> scenarios = businessScenarioRepository.findAll();
        model.addAttribute("scenarios", scenarios);
        model.addAttribute("role", session.getAttribute("role"));

        model.addAttribute("selectedScenarioId", scenarioId); // You can use this in the form

        return "addscenariotransaction";
    }

    @PostMapping("/add")
    public ResponseEntity<?> createTransaction(@RequestBody ScenariotransactionDTO dto) {
        String parentCode = dto.getBusinessScenario();     // e.g., "01"
        String childCode = dto.getTransactionSuffix();     // e.g., "02"

        if (parentCode == null || parentCode.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing or empty businessScenario");
        }

        if (childCode == null || childCode.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing or empty transactionSuffix");
        }

        String fullCode = parentCode + childCode;          // e.g., "0102"

        // ✅ Check if transactionKey already exists
        if (scenarioTransactionRepository.existsById(fullCode)) {
            return ResponseEntity.status(409).body("Transaction already exists with key: " + fullCode);
        }

        // ✅ Fetch parent business scenario
        BusinessScenario scenario = businessScenarioRepository.findById(parentCode)
            .orElseThrow(() -> new RuntimeException("Invalid BusinessScenario ID: " + parentCode));

        // ✅ Create and populate entity
        Scenariotransaction transaction = new Scenariotransaction();
        transaction.setTransactionKey(fullCode);
        transaction.setTransactionSuffix(childCode);
        transaction.setBusinessScenario(scenario);
        transaction.setScenarioDescription(dto.getScenarioDescription());
        transaction.setWorkStream(dto.getWorkStream());
        transaction.setActivity(dto.getActivity());
        transaction.setResponsible(dto.getResponsible());
        transaction.setExpectedOutcome(dto.getExpectedOutcome());
        transaction.setTcode(dto.getTcode());

        // ✅ Save
        scenarioTransactionRepository.save(transaction);

        return ResponseEntity.ok("Transaction saved with key: " + fullCode);
    }

    
   



    // ✅ List transactions for a specific scenario
    @GetMapping("/transactions/{scenarioId}")
    public String listTransactions(@PathVariable("scenarioId") String scenarioId, Model model) {
        List<ScenariotransactionDTO> transactions = transactionService.getTransactionsByBusinessScenario(scenarioId);
        BusinessScenario scenario = businessScenarioRepository.findById(scenarioId).orElse(null);

        model.addAttribute("scenario", scenario);
        model.addAttribute("transactions", transactions);

        return "transactionlist"; // A separate Thymeleaf page to list transactions
    }

    // ✅ Delete all transactions for a scenario
    @PostMapping("/transactions/delete/{scenarioId}")
    public String deleteTransactions(@PathVariable("scenarioId") String scenarioId) {
        transactionService.deleteTransactionsByScenario(scenarioId);
        return "redirect:/transactions/" + scenarioId + "?deleted";
    }
    
    @GetMapping("/addtransactions/addform")
    public String showTransactionFormForScenario(@RequestParam("scenarioId") String scenarioId, Model model) {
        ScenariotransactionDTO dto = new ScenariotransactionDTO();
        dto.setBusinessScenario(scenarioId);
        model.addAttribute("transaction", dto);
        return "addscenariotransaction";  // your Thymeleaf form view
    }

    
    @GetMapping("/scenarios")
    @ResponseBody
    public List<ScenariotransactionDTO> getAllScenariosAsDTOs() {
        return transactionService.getAllScenariosAsDTOs();
    }
    @GetMapping("/scenarioss")
    public String getAllScenariosView(Model model) {
        List<ScenariotransactionDTO> scenarioList = transactionService.getAllScenariosAsDTOs();
        model.addAttribute("scenarios", scenarioList);
        return "scenariotransactionlist"; // Name of your Thymeleaf template: scenarioList.html
    }
    
    @GetMapping("/scenario-list")
    public String listAllScenarios(Model model, @RequestParam(value = "editId", required = false) String editId) {
        List<ScenariotransactionDTO> allScenarios = transactionService.getAllScenarios();
        model.addAttribute("scenarios", allScenarios);
        model.addAttribute("editId", editId); // pass current editing row id
        return "headerscenariolist";
    }

    @GetMapping("/getBySuffix/{suffix}")
    @ResponseBody
    public ResponseEntity<ScenariotransactionDTO> getByTransactionSuffix(@PathVariable("suffix") String suffix) {
        return scenarioTransactionRepository.findById(suffix)
            .map(entity -> {
                ScenariotransactionDTO dto = new ScenariotransactionDTO();
                dto.setBusinessScenario(entity.getBusinessScenario().getBusinessScenario());
                dto.setTransactionSuffix(entity.getTransactionSuffix());
                dto.setScenarioDescription(entity.getScenarioDescription());
                dto.setWorkStream(entity.getWorkStream());
                dto.setActivity(entity.getActivity());
                dto.setResponsible(entity.getResponsible());
                dto.setExpectedOutcome(entity.getExpectedOutcome());
                dto.setTcode(entity.getTcode());
                dto.setTransactionKey(entity.getTransactionKey());
                return ResponseEntity.ok(dto);
            }).orElse(ResponseEntity.notFound().build());
    }
    
    @RequestMapping("/api/transactions")
    public class TransactionController {

        @Autowired
        private ScenarioTransactionService scenarioTransactionService;

    }
    
    @GetMapping("/businessScenarioTransactions")
    public String showBusinessScenarioTransactionsPage() {
    	return "bussinesscenarioandtransactions";
    }

    @GetMapping("/all-data")
    @ResponseBody
    public List<Map<String, Object>> getAllData() {
        return transactionService.getAllScenarioTransactions();
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateTransaction(@RequestBody ScenariotransactionDTO dto) {
        try {
            Scenariotransaction entity = scenarioTransactionRepository.findById(dto.getTransactionKey())
                    .orElseThrow(() -> new RuntimeException("Transaction not found"));

            entity.setScenarioDescription(dto.getScenarioDescription());
            entity.setWorkStream(dto.getWorkStream());
            entity.setActivity(dto.getActivity());
            entity.setResponsible(dto.getResponsible());
            entity.setExpectedOutcome(dto.getExpectedOutcome());
            entity.setTcode(dto.getTcode());
            entity.setTransactionSuffix(dto.getTransactionSuffix());

            scenarioTransactionRepository.save(entity);

            return ResponseEntity.ok(entity);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Update failed: " + e.getMessage());
        }
    }

}
