package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.CityRef;
import com.example.demo.entity.CountryRef;
import com.example.demo.entity.LanguageRef;
import com.example.demo.entity.StateRef;
import com.example.demo.repo.CityRefRepository;
import com.example.demo.repo.CountryRefRepository;
import com.example.demo.repo.LanguageRefRepository;
import com.example.demo.repo.StateRefRepository;

@Service
public class StateRefService {

    @Autowired
    private StateRefRepository repo;
    
    @Autowired
    private CityRefRepository cityrepo;
    
    
    @Autowired
    private LanguageRefRepository repository;
    
    @Autowired
    private CountryRefRepository countryrepo;

    public List<StateRef> getAllStates() {
        return repo.findAll();
    }
    
    
    public List<CountryRef> getCountries() {
        return countryrepo.findAll();
    }
    

    public List<CityRef> getAllCities() {
        return cityrepo.findAll();
    }
    
    
    
    public StateRef getStateByCode(String stateCode) {
        return repo.findById(stateCode)
                   .orElseThrow(() -> new IllegalArgumentException("State not found: " + stateCode));
    }


    public StateRef updateState(String stateCode, StateRef updatedState) {
        StateRef existing = repo.findById(stateCode)
                .orElseThrow(() -> new IllegalArgumentException("State not found: " + stateCode));

        existing.setCountry(updatedState.getCountry());
        existing.setCountryCode(updatedState.getCountryCode());
        existing.setStateSubdivisionName(updatedState.getStateSubdivisionName());

        return repo.save(existing);
    }




	public void updateStateByCode(String stateCode, StateRef updatedState) {
		// TODO Auto-generated method stub
		
	}

	
	public CityRef getCityByName(String city) {
        return cityrepo.findById(city).orElse(null);
    }

    // Update city
    public void updateCity(CityRef city) {
    	cityrepo.save(city);
    }

    
    public void saveCity(CityRef cityRef) {
    	cityrepo.save(cityRef);
    }
    
   

    public List<LanguageRef> getAllLanguages() {
        return repository.findAll();
    }
    
    public LanguageRef saveLanguage(LanguageRef languageRef) {
        return repository.save(languageRef);
    }

    public LanguageRef getLanguageByCode(String code) {
        return repository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Language not found: " + code));
    }

    public LanguageRef updateLanguage(String code, LanguageRef updated) {
        LanguageRef existing = getLanguageByCode(code);
        existing.setLanguage(updated.getLanguage());
        return repository.save(existing);
    }

    public void deleteLanguage(String code) {
        repository.deleteById(code);
    }
    
    // Country methods
    public CountryRef getCountryByCode(String countryCode) {
        return countryrepo.findById(countryCode)
                .orElseThrow(() -> new IllegalArgumentException("Country not found: " + countryCode));
    }
    
    public CountryRef saveCountry(CountryRef country) {
        return countryrepo.save(country);
    }
    
    public CountryRef updateCountry(String countryCode, CountryRef updatedCountry) {
        CountryRef existing = countryrepo.findById(countryCode)
                .orElseThrow(() -> new IllegalArgumentException("Country not found: " + countryCode));
        
        existing.setCountryName(updatedCountry.getCountryName());
        existing.setNumericCode(updatedCountry.getNumericCode());
        
        return countryrepo.save(existing);
    }

    public void deleteCountry(String countryCode) {
        countryrepo.deleteById(countryCode);
    }

    // State methods
    public StateRef saveState(StateRef state) {
        return repo.save(state);
    }

    public void deleteState(String stateCode) {
        repo.deleteById(stateCode);
    }

    // City methods
    public void deleteCity(String city) {
        cityrepo.deleteById(city);
    }
}
