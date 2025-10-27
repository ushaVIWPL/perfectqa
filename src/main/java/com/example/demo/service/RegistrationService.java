package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.CountryRef;
import com.example.demo.repo.CountryRefRepository;

@Service
public class RegistrationService {

    private List<CountryRef> cachedCountries;


	
	
	@Autowired
	private final CountryRefRepository countryRepo;

    public RegistrationService(CountryRefRepository countryRepo) {
        this.countryRepo = countryRepo;
    }



    public List<CountryRef> getCountries() {
        if (cachedCountries == null || cachedCountries.isEmpty()) {
            System.out.println("Fetching countries from DB...");
            cachedCountries = countryRepo.findAll();

            // Print each country to console
            for (CountryRef country : cachedCountries) {
                System.out.println("Country: " + country.getCountryName() + " | Code: " + country.getCountryCode());
            }
        } else {
            System.out.println("Returning countries from cache.");
        }

        return cachedCountries;
    }
	
}
