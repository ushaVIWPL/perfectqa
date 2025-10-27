package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.CityRef;
import com.example.demo.entity.CountryRef;
import com.example.demo.entity.StateRef;
import com.example.demo.entity.UserAccount;
import com.example.demo.service.StateRefService;

@Controller
@RequestMapping
public class StateRefController {

    @Autowired
    private StateRefService stateService;
  


    // ==========================
    // STATES MODULE
    // ==========================

    // ✅ Display all states
    @GetMapping("/admin/states")
    public String getStates(Model model) {
        model.addAttribute("states", stateService.getAllStates());
        return "states"; // Thymeleaf template: states.html
    }

    // ✅ Get a single state for edit form
    @GetMapping("/states/edit/{stateCode}")
    public String editState(@PathVariable String stateCode, Model model) {
        StateRef state = stateService.getStateByCode(stateCode); // call service to fetch state
        model.addAttribute("state", state);
        return "states"; // Thymeleaf template: edit-state.html
    }


    @PostMapping("/states/update/{code}")
    public String updateState(@PathVariable String code, @ModelAttribute StateRef stateRef) {
        stateService.updateState(code, stateRef); // save changes
        return "redirect:/admin/states"; // reload list page
    }

 

    // ==========================
    // CITIES MODULE
    // ==========================

    @GetMapping("/admin/cities")
    public String getCities(Model model) {
        model.addAttribute("cities", stateService.getAllCities());
        return "city";
    }

    
    // ✅ Show edit form for a specific city
    @GetMapping("/edit/{city}")
    public String editCityForm(@PathVariable String city, Model model) {
        CityRef cityRef = stateService.getCityByName(city);
        model.addAttribute("cityRef", cityRef);
        return "city-edit"; // Thymeleaf template
    }


    // ✅ Handle form submission and update the record
    @PostMapping("/admin/update")
    public String updateCity(@ModelAttribute CityRef cityRef) {
        stateService.saveCity(cityRef); // save updated city
        return "redirect:/admin/cities"; // reload the list page
    }

    
    
//    country ref
    
    
    @GetMapping("/admin/country")
    public String showCountryForm(Model model) {
        List<CountryRef> countries = stateService.getCountries();

        model.addAttribute("countries", countries);
        return "country"; // matches the HTML page you posted
    }
    
    
    
    @GetMapping("/admin/languages")
    public String getLanguages(Model model) {
        model.addAttribute("languages", stateService.getAllLanguages());
        return "languages"; // ✅ Thymeleaf page name (languages.html)
    }
    

}
