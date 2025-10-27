package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.BusinessScenario;
import com.example.demo.service.BusinessScenarioService;
import com.example.demo.service.ScenarioTransactionService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	@Autowired
	private BusinessScenarioService businessScenarioService;

	@Autowired
	private ScenarioTransactionService scenarioTransactionservice;
    
	@GetMapping("/")
	public String showDashboard() {
	    return "dashboard";
	}

	
	/*
	 * @GetMapping("/admin") public String showadminDashboard() { return "admin"; }
	 */

	
	
	@GetMapping("/createticket")
	public String showTickets() {
	    return "ticket_form";
	}
	

    @GetMapping("/addheadertestcase")
    public String showScenarioForm(Model model) {
        model.addAttribute("header", new BusinessScenario());
        return "addheaderscenario";  // Loads form.html inside iframe
    }

 
    @GetMapping("/loginform")
    public String showLoginForm() {
        return "loginForm";  // Loads form.html inside iframe
    }

	

  

    

    @GetMapping("/home")
    public String home(Model model, @ModelAttribute("logoutMessage") String logoutMessage) {
        model.addAttribute("logoutMessage", logoutMessage);
        return "home"; // your home.html view
    }

 
	/*
	 * @GetMapping("/Menu") public String showQaAdminMenu() { return "qatestermenu";
	 * // Name of the Thymeleaf HTML file without `.html` }
	 * 
	 */
    
   
    
    @GetMapping("/Menu")
    public String showQaAdminMenu(HttpSession session) {
        if(session.getAttribute("username") == null) {
            session.setAttribute("username", "User");
        }
        if(session.getAttribute("role") == null) {
            session.setAttribute("role", "GUEST");
        }
        return "qatestermenu";
    }

    
    
    
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();  // clear session
        redirectAttributes.addFlashAttribute("logoutMessage", "You have successfully logged out!");
        return "redirect:/home";  // redirect to home page
    }



//    
//    
////    Scenario saction Methods
//    
////
////    @GetMapping("/scenariotralist")
////    public String getAllTransactions(Model model) {
////        model.addAttribute("scenarios", scenarioTransactionservice.getAllScenarios());
////        return "scenariotransactionlist"; // Thymeleaf template name
////    }
////
////    
//    
//    
//    @GetMapping("/addscenariotransaction")
//    public String showForm(Model model) {
//        model.addAttribute("scenario", new Scenariotransaction());
//        return "addscenariotransaction";
//    }
//    
//    
////testheader
//    
//    @GetMapping("/addtestheader")
//    public String showAddForm(Model model) {
//        model.addAttribute("testcase", new TestCaseHeader());
//        return "addtestheader";
//    }
//    
//    @GetMapping("/listtestheader")
//    public String showListForm(Model model) {
//        model.addAttribute("testcase", new TestCaseHeader());
//        return "testheaderlist";
//    }

}
