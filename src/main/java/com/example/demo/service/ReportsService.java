/*
 * package com.example.demo.service;
 * 
 * import java.util.ArrayList; import java.util.List;
 * 
 * import org.springframework.stereotype.Service;
 * 
 * import com.example.demo.dto.ScenarioResultDTO; import
 * com.example.demo.injections.ScenarioResultProjection; import
 * com.example.demo.repo.ReportsRepository;
 * 
 * import jakarta.persistence.EntityManager; import
 * jakarta.persistence.PersistenceContext; import
 * jakarta.persistence.StoredProcedureQuery;
 * 
 * @Service public class ReportsService {
 * 
 * private final ReportsRepository reportsRepo;
 * 
 * public ReportsService(ReportsRepository reportsRepo) { this.reportsRepo =
 * reportsRepo; }
 * 
 * public List<ScenarioResultDTO> getReport() { List<ScenarioResultProjection>
 * projections = reportsRepo.fetchScenarioReport(); List<ScenarioResultDTO>
 * result = new ArrayList<>();
 * 
 * for (ScenarioResultProjection p : projections) { ScenarioResultDTO dto = new
 * ScenarioResultDTO(); dto.setBusinessScenario(p.getBusinessScenario());
 * dto.setActivity(p.getActivity());
 * dto.setExpectedOutcome(p.getExpectedOutcome());
 * dto.setResponsible(p.getResponsible());
 * dto.setScenarioDescription(p.getScenarioDescription());
 * dto.setWorkStream(p.getWorkStream());
 * dto.setTransactionKey(p.getTransactionKey());
 * dto.setTransActivity(p.getTransActivity());
 * dto.setTransTransactionKey(p.getTransTransactionKey());
 * dto.setTransExpectedOutcome(p.getTransExpectedOutcome());
 * dto.setTransResponsible(p.getTransResponsible());
 * dto.setTransScenarioDescription(p.getTransScenarioDescription());
 * dto.setTcode(p.getTcode());
 * dto.setTransactionSuffix(p.getTransactionSuffix());
 * dto.setTransWorkStream(p.getTransWorkStream());
 * dto.setCombinedKey(p.getCombinedKey()); dto.setTcActivity(p.getTcActivity());
 * dto.setDescription(p.getDescription()); dto.setEndDate(p.getEndDate());
 * dto.setTcExpectedOutcome(p.getTcExpectedOutcome());
 * dto.setNavigateSteps(p.getNavigateSteps());
 * dto.setPrerequisites(p.getPrerequisites());
 * dto.setTcResponsible(p.getTcResponsible()); dto.setScenario(p.getScenario());
 * dto.setScreenShotjpg(p.getScreenShotjpg());
 * dto.setStartDate(p.getStartDate());
 * dto.setSuccessCriteria(p.getSuccessCriteria());
 * dto.setTestData(p.getTestData()); dto.setTestedBy(p.getTestedBy());
 * dto.setTcTransactionKey(p.getTcTransactionKey());
 * dto.setTcWorkStream(p.getTcWorkStream()); result.add(dto); }
 * 
 * return result; }
 * 
 * 
 * 
 * 
 * @PersistenceContext private EntityManager entityManager;
 * 
 * public List<ScenarioResultDTO> getDetailedTransactions() {
 * StoredProcedureQuery query = entityManager
 * .createStoredProcedureQuery("SP_DetailedTestCaseTransaction");
 * 
 * query.execute(); List<Object[]> results = query.getResultList();
 * 
 * return results.stream().map(r -> { ScenarioResultDTO dto = new
 * ScenarioResultDTO(); int i = 0;
 * 
 * dto.setBusinessScenario(String.valueOf(r[i++]));
 * dto.setActivity(String.valueOf(r[i++]));
 * dto.setExpectedOutcome(String.valueOf(r[i++]));
 * dto.setResponsible(String.valueOf(r[i++]));
 * dto.setScenarioDescription(String.valueOf(r[i++]));
 * dto.setWorkStream(String.valueOf(r[i++]));
 * 
 * dto.setTransactionKey(String.valueOf(r[i++]));
 * dto.setTransActivity(String.valueOf(r[i++]));
 * dto.setTransTransactionKey(String.valueOf(r[i++]));
 * dto.setTransExpectedOutcome(String.valueOf(r[i++]));
 * dto.setTransResponsible(String.valueOf(r[i++]));
 * dto.setTransScenarioDescription(String.valueOf(r[i++]));
 * dto.setTcode(String.valueOf(r[i++]));
 * dto.setTransactionSuffix(String.valueOf(r[i++]));
 * dto.setTransWorkStream(String.valueOf(r[i++]));
 * 
 * dto.setCombinedKey(String.valueOf(r[i++]));
 * dto.setTcActivity(String.valueOf(r[i++]));
 * dto.setDescription(String.valueOf(r[i++]));
 * dto.setEndDate(String.valueOf(r[i++]));
 * dto.setTcExpectedOutcome(String.valueOf(r[i++]));
 * dto.setNavigateSteps(String.valueOf(r[i++]));
 * dto.setPrerequisites(String.valueOf(r[i++]));
 * dto.setTcResponsible(String.valueOf(r[i++]));
 * dto.setScenario(String.valueOf(r[i++]));
 * dto.setScreenShotjpg(String.valueOf(r[i++]));
 * dto.setStartDate(String.valueOf(r[i++]));
 * dto.setSuccessCriteria(String.valueOf(r[i++]));
 * dto.setTestData(String.valueOf(r[i++]));
 * dto.setTestedBy(String.valueOf(r[i++]));
 * dto.setTcTransactionKey(String.valueOf(r[i++]));
 * dto.setTcWorkStream(String.valueOf(r[i++]));
 * 
 * dto.setTransactionId(String.valueOf(r[i++]));
 * dto.setTestCaseNo(String.valueOf(r[i++]));
 * dto.setMainKey(String.valueOf(r[i++])); //
 * dto.setSerialNo(String.valueOf(r[i++])); // you said to remove serial no
 * 
 * dto.setType(String.valueOf(r[i++])); dto.setAction(String.valueOf(r[i++]));
 * dto.setUrl(String.valueOf(r[i++]));
 * dto.setExpectedResults(String.valueOf(r[i++]));
 * dto.setActualResults(String.valueOf(r[i++]));
 * dto.setTestedDate(String.valueOf(r[i++]));
 * dto.setPassFail(String.valueOf(r[i++]));
 * dto.setDefects(String.valueOf(r[i++]));
 * dto.setComments(String.valueOf(r[i++]));
 * dto.setScreenshotPaths(String.valueOf(r[i++]));
 * 
 * return dto; }).toList(); } }
 * 
 * 
 */