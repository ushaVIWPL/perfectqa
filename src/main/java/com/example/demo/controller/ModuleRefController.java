package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.ApplicationRef;
import com.example.demo.entity.ModuleRef;
import com.example.demo.entity.UserAccount;
import com.example.demo.repo.Applicationrefrepo;
import com.example.demo.repo.ModuleRefRepository;
import com.example.demo.repo.UserAccountRepository;
import com.example.demo.service.ModuleRefService;
import com.example.demo.service.RegistrationService;
import com.example.demo.service.UserAccountService;

@Controller
public class ModuleRefController {

    @Autowired
    private ModuleRefRepository moduleRefRepository;

    @Autowired
    private Applicationrefrepo applicationrefrepo;

    @Autowired
    private UserAccountRepository userAccountRepository;
    
    @Autowired
 private   UserAccountService service;
    @Autowired
   private  RegistrationService registrationService;

    
    @Autowired
    private Applicationrefrepo applicationRepo;
    
    @Autowired
    private ModuleRefService moduleRefService;
    // ✅ Handle form submission (POST)
    @PostMapping("/saveModule")
    public String saveModule(
            @RequestParam(required = false) String leadTester,
            @RequestParam(required = false) String tester1,
            @RequestParam(required = false) String tester2,
            @RequestParam(required = false) String tester3,
            @RequestParam(required = false) String tester4,
            @RequestParam(required = false) String tester5,
            @RequestParam(required = false) String tester6,
            @RequestParam(required = false) String tester7,
            @RequestParam(required = false) String tester8,
            @RequestParam(required = false) String moduleStatus,
            @RequestParam(required = false) String moduleDev,
            @RequestParam(required = false) String moduleBusinessLead,
            @RequestParam(required = false) String moduleBusinessAdvisor,
            @RequestParam(required = false) String moduleArchitect,
            @RequestParam(required = false) String moduleDBA,
            @RequestParam(required = false) String moduleExcelLead,
            @RequestParam(required = false) String moduleFunctionLead,
            @RequestParam(required = false) String moduleTechnicalLead,
            @RequestParam(required = false) String moduleStartDateActual,
            @RequestParam(required = false) String moduleEndDateEst,
            @RequestParam(required = false) String moduleEndDateActual,
            @RequestParam String appCode
    ) {
        // 🧠 Generate next module key automatically
        String nextModuleKey = generateNextModuleKey();

        ModuleRef module = new ModuleRef();
        module.setModuleKey(nextModuleKey);
        module.setLeadTester(leadTester);
        module.setTester1(tester1);
        module.setTester2(tester2);
        module.setTester3(tester3);
        module.setTester4(tester4);
        module.setTester5(tester5);
        module.setTester6(tester6);
        module.setTester7(tester7);
        module.setTester8(tester8);
        module.setModuleStatus(moduleStatus);
        module.setModuleDev(moduleDev);

        module.setModuleBusinessLead(moduleBusinessLead);
        module.setModuleBusinessAdvisor(moduleBusinessAdvisor);
        module.setModuleArchitect(moduleArchitect);
        module.setModuleDBA(moduleDBA);
        module.setModuleExcelLead(moduleExcelLead);
        module.setModuleFunctionLead(moduleFunctionLead);
        module.setModuleTechnicalLead(moduleTechnicalLead);

        module.setModuleStartDateActual(moduleStartDateActual);
        module.setModuleEndDateEst(moduleEndDateEst);
        module.setModuleEndDateActual(moduleEndDateActual);

        // 🔗 Link with ApplicationRef
        ApplicationRef app = applicationrefrepo.findById(appCode).orElse(null);
        if (app != null) {
            module.setApplication(app);
        }

        moduleRefRepository.save(module);

        return "redirect:/aaddModule";
    }

    // ✅ Handle redirect (GET)
 
    
    
    
	/*
	 * @GetMapping("/modules") public String showModules(Model model) {
	 * List<ModuleRef> modules = moduleRefService.getAllModules();
	 * System.out.println("Modules fetched: " + modules.size());
	 * model.addAttribute("modules", modules); return "addModule"; // your HTML file
	 * }
	 */

    
    @GetMapping("/api/modules")
    @ResponseBody
    public List<ModuleRef> getModules() {
        return moduleRefService.getAllModules();
    }

    @GetMapping("/aaddModule")
    public String showModules(
            @RequestParam(required = false) String search,
            Model model) {
        List<ModuleRef> modules;
        
        // If search term is provided, search by company name or code
        if (search != null && !search.trim().isEmpty()) {
            modules = moduleRefService.searchByCompanyNameOrCode(search.trim());
        } else {
            modules = moduleRefService.getAllModules();
        }
        
        model.addAttribute("modules", modules);
        List<UserAccount> users = service.getAllUsers(); 
        model.addAttribute("users", users); // passing the list to Thymeleaf        
        model.addAttribute("applications", applicationRepo.findAll());
        model.addAttribute("application", new ApplicationRef());
        model.addAttribute("searchTerm", search != null ? search : "");
        return "addModule";  // Your HTML (Thymeleaf) page name
    }
    
    
    
    // 🧩 Helper method: Generate next module key
    private String generateNextModuleKey() {
        // Fetch the latest module key
        ModuleRef lastModule = moduleRefRepository.findTopByOrderByModuleKeyDesc();

        if (lastModule == null || lastModule.getModuleKey() == null) {
            return "MOD001"; // first key
        }

        // Extract numeric part (e.g., "MOD007" → 7)
        String lastKey = lastModule.getModuleKey();
        int num = Integer.parseInt(lastKey.replace("MOD", ""));
        num++; // increment

        // Return new formatted key
        return String.format("MOD%03d", num);
    }
    
    @GetMapping("/modules/delete/{moduleKey}")
    public String deleteModule(@PathVariable String moduleKey) {
        moduleRefService.deleteModule(moduleKey);
        return "redirect:/aaddModule";
        
        
    }

 

    @GetMapping("/modules/get/{moduleKey}")
    @ResponseBody
    public ResponseEntity<?> getModuleByKey(@PathVariable String moduleKey) {
        Optional<ModuleRef> module = moduleRefRepository.findById(moduleKey);
        return module.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }


    
    @PostMapping("/saveupdateModule")
    public String saveModule(@ModelAttribute ModuleRef module, @RequestParam(required = false) String appCode) {
        // For new module, generate key
        if (module.getModuleKey() == null || module.getModuleKey().isEmpty()) {
            String nextModuleKey = generateNextModuleKey();
            module.setModuleKey(nextModuleKey);
        }

        // Link with ApplicationRef
        if (appCode != null) {
            ApplicationRef app = applicationrefrepo.findById(appCode).orElse(null);
            module.setApplication(app);
        }

        moduleRefRepository.save(module);
        return "redirect:/aaddModule";
    }


    @GetMapping("/modules/edit/{moduleKey}")
    public String editModule(@PathVariable String moduleKey, Model model) {
        ModuleRef module = moduleRefRepository.findById(moduleKey)
            .orElseThrow(() -> new IllegalArgumentException("Invalid module key: " + moduleKey));
        model.addAttribute("module", module);
        return "editModule";  // matches file name
    }


}
