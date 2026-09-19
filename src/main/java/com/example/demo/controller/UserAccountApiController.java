/*
 * package com.example.demo.controller;
 * 
 * 
 * import java.util.List;
 * 
 * import org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.web.bind.annotation.CrossOrigin; import
 * org.springframework.web.bind.annotation.GetMapping; import
 * org.springframework.web.bind.annotation.PathVariable; import
 * org.springframework.web.bind.annotation.RequestMapping; import
 * org.springframework.web.bind.annotation.RestController;
 * 
 * import com.example.demo.entity.BusinessScenario; import
 * com.example.demo.entity.ScenarioActivities; import
 * com.example.demo.service.BusinessScenarioService; import
 * com.example.demo.service.ScenarioActivitiesService;
 * 
 * @RestController
 * 
 * @RequestMapping("/api/business-scenarios")
 * 
 * @CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
 * public class UserAccountApiController {
 * 
 * @Autowired private BusinessScenarioService scenarioService;
 * 
 * @Autowired private ScenarioActivitiesService activitiesService;
 * 
 * // ✅ GET ALL BUSINESS SCENARIOS
 * 
 * @GetMapping public List<BusinessScenario> getAllScenarios() { return
 * scenarioService.getAllScenarios(); }
 * 
 * // ✅ GET BUSINESS SCENARIOS BY COMPANY
 * 
 * @GetMapping("/company/{companyCode}") public List<BusinessScenario>
 * getScenariosByCompany(
 * 
 * @PathVariable String companyCode) { return
 * scenarioService.getScenariosByCompany(companyCode); }
 * 
 * // ✅ GET ALL ACTIVITIES (SEPARATE PATH)
 * 
 * @GetMapping("/activities") public List<ScenarioActivities> getAllActivities()
 * { return activitiesService.getAllActivities(); }
 * 
 * // ✅ GET ACTIVITIES BY COMPANY
 * 
 * @GetMapping("/activities/company/{companyCode}") public
 * List<ScenarioActivities> getActivitiesByCompany(
 * 
 * @PathVariable String companyCode) {
 * 
 * companyCode = companyCode.trim().toUpperCase();
 * 
 * return activitiesService.getActivitiesByCompanyCode(companyCode); }
 * 
 * }
 * 
 */