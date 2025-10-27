package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.service.RegistrationService;


@Controller
public class RegistrationController {

	@Autowired
	private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }
	/*
	 * @GetMapping("/register") public String showCountries(Model model) {
	 * model.addAttribute("countries", registrationService.getCountries()); return
	 * "registrationForm"; }
	 * 
	 */
    
	

	
	
	
	
	
	
	
}
