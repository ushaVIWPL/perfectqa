package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.CityRef;
import com.example.demo.entity.CountryRef;
import com.example.demo.entity.LanguageRef;
import com.example.demo.entity.StateRef;
import com.example.demo.service.StateRefService;
import com.example.demo.util.RoleAccessUtil;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping
public class StateRefController {

    @Autowired
    private StateRefService stateService;

    // ==========================
    // COUNTRIES MODULE
    // ==========================

    @GetMapping({"/admin/country", "/admin/countries"})
    public String showCountryForm(Model model, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return "redirect:/login";
        }
        List<CountryRef> countries = stateService.getCountries();
        model.addAttribute("countries", countries);
        return "country";
    }

    @PostMapping({"/admin/country/add", "/admin/countries/add"})
    @ResponseBody
    public ResponseEntity<?> addCountry(@RequestBody CountryRef country, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            CountryRef saved = stateService.saveCountry(country);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding country: " + e.getMessage());
        }
    }

    @GetMapping({"/admin/country/get/{code}", "/admin/countries/get/{code}", "/admin/country/edit/{code}", "/admin/countries/edit/{code}"})
    @ResponseBody
    public ResponseEntity<?> getCountryForEdit(@PathVariable String code, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            CountryRef country = stateService.getCountryByCode(code);
            return ResponseEntity.ok(country);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Country not found");
        }
    }

    @PutMapping({"/admin/country/update/{code}", "/admin/countries/update/{code}"})
    @PostMapping({"/admin/country/update/{code}", "/admin/countries/update/{code}"})
    @ResponseBody
    public ResponseEntity<String> updateCountry(@PathVariable String code, @RequestBody CountryRef updatedCountry, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            stateService.updateCountry(code, updatedCountry);
            return ResponseEntity.ok("Country updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping({"/admin/country/delete/{code}", "/admin/countries/delete/{code}"})
    @PostMapping({"/admin/country/delete/{code}", "/admin/countries/delete/{code}"})
    @ResponseBody
    public ResponseEntity<String> deleteCountry(@PathVariable String code, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            stateService.deleteCountry(code);
            return ResponseEntity.ok("Country deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Cannot delete country: It may be referenced by states or cities.");
        }
    }

    // ==========================
    // STATES MODULE
    // ==========================

    @GetMapping({"/admin/state", "/admin/states"})
    public String getStates(Model model, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("states", stateService.getAllStates());
        model.addAttribute("countries", stateService.getCountries());
        return "states";
    }

    @PostMapping({"/admin/state/add", "/admin/states/add"})
    @ResponseBody
    public ResponseEntity<?> addState(@RequestBody StateRef state, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            StateRef saved = stateService.saveState(state);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding state: " + e.getMessage());
        }
    }

    @GetMapping({"/admin/state/get/{code}", "/admin/states/get/{code}", "/admin/state/edit/{code}", "/admin/states/edit/{code}", "/states/edit/{code}"})
    @ResponseBody
    public ResponseEntity<?> getStateForEdit(@PathVariable String code, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            StateRef state = stateService.getStateByCode(code);
            return ResponseEntity.ok(state);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("State not found");
        }
    }

    @PostMapping({"/admin/state/update/{code}", "/admin/states/update/{code}", "/states/update/{code}"})
    @PutMapping({"/admin/state/update/{code}", "/admin/states/update/{code}", "/states/update/{code}"})
    @ResponseBody
    public ResponseEntity<String> updateState(@PathVariable String code, @RequestBody StateRef stateRef, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            stateService.updateState(code, stateRef);
            return ResponseEntity.ok("State updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating state: " + e.getMessage());
        }
    }

    @DeleteMapping({"/admin/state/delete/{code}", "/admin/states/delete/{code}"})
    @PostMapping({"/admin/state/delete/{code}", "/admin/states/delete/{code}"})
    @ResponseBody
    public ResponseEntity<String> deleteState(@PathVariable String code, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            stateService.deleteState(code);
            return ResponseEntity.ok("State deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Cannot delete state: It may be referenced by cities.");
        }
    }

    // ==========================
    // CITIES MODULE
    // ==========================

    @GetMapping({"/admin/city", "/admin/cities"})
    public String getCities(Model model, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("cities", stateService.getAllCities());
        model.addAttribute("states", stateService.getAllStates());
        model.addAttribute("countries", stateService.getCountries());
        return "city";
    }

    @PostMapping({"/admin/city/add", "/admin/cities/add"})
    @ResponseBody
    public ResponseEntity<?> addCity(@RequestBody CityRef city, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            stateService.saveCity(city);
            return ResponseEntity.ok("City added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding city: " + e.getMessage());
        }
    }

    @GetMapping({"/admin/city/get/{city}", "/admin/cities/get/{city}", "/admin/city/edit/{city}", "/admin/cities/edit/{city}", "/edit/{city}"})
    @ResponseBody
    public ResponseEntity<?> getCityForEdit(@PathVariable String city, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            CityRef cityRef = stateService.getCityByName(city);
            if (cityRef == null) {
                return ResponseEntity.badRequest().body("City not found");
            }
            return ResponseEntity.ok(cityRef);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving city");
        }
    }

    @PostMapping({"/admin/city/update/{city}", "/admin/cities/update/{city}", "/admin/update"})
    @PutMapping({"/admin/city/update/{city}", "/admin/cities/update/{city}"})
    @ResponseBody
    public ResponseEntity<String> updateCity(@RequestBody CityRef cityRef, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            stateService.saveCity(cityRef);
            return ResponseEntity.ok("City updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating city: " + e.getMessage());
        }
    }

    @DeleteMapping({"/admin/city/delete/{city}", "/admin/cities/delete/{city}"})
    @PostMapping({"/admin/city/delete/{city}", "/admin/cities/delete/{city}"})
    @ResponseBody
    public ResponseEntity<String> deleteCity(@PathVariable String city, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            stateService.deleteCity(city);
            return ResponseEntity.ok("City deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting city: " + e.getMessage());
        }
    }

    // ==========================
    // LANGUAGES MODULE
    // ==========================

    @GetMapping({"/admin/language", "/admin/languages"})
    public String getLanguages(Model model, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return "redirect:/login";
        }
        model.addAttribute("languages", stateService.getAllLanguages());
        return "languages";
    }

    @PostMapping({"/admin/language/add", "/admin/languages/add"})
    @ResponseBody
    public ResponseEntity<?> addLanguage(@RequestBody LanguageRef language, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            LanguageRef saved = stateService.saveLanguage(language);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding language: " + e.getMessage());
        }
    }

    @GetMapping({"/admin/language/get/{code}", "/admin/languages/get/{code}"})
    @ResponseBody
    public ResponseEntity<?> getLanguageForEdit(@PathVariable String code, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            LanguageRef language = stateService.getLanguageByCode(code);
            return ResponseEntity.ok(language);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Language not found");
        }
    }

    @PostMapping({"/admin/language/update/{code}", "/admin/languages/update/{code}"})
    @PutMapping({"/admin/language/update/{code}", "/admin/languages/update/{code}"})
    @ResponseBody
    public ResponseEntity<String> updateLanguage(@PathVariable String code, @RequestBody LanguageRef updatedLanguage, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            stateService.updateLanguage(code, updatedLanguage);
            return ResponseEntity.ok("Language updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating language: " + e.getMessage());
        }
    }

    @DeleteMapping({"/admin/language/delete/{code}", "/admin/languages/delete/{code}"})
    @PostMapping({"/admin/language/delete/{code}", "/admin/languages/delete/{code}"})
    @ResponseBody
    public ResponseEntity<String> deleteLanguage(@PathVariable String code, HttpSession session) {
        if (!RoleAccessUtil.isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        try {
            stateService.deleteLanguage(code);
            return ResponseEntity.ok("Language deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting language: " + e.getMessage());
        }
    }
}
