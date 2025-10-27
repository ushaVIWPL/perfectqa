package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "StateRef")
public class StateRef {

    @Id
    private String stateSubdivisionCode;

    private String country;
    private String countryCode;
    private String stateSubdivisionName;

    // Getters and setters
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getStateSubdivisionName() {
        return stateSubdivisionName;
    }

    public void setStateSubdivisionName(String stateSubdivisionName) {
        this.stateSubdivisionName = stateSubdivisionName;
    }

    public String getStateSubdivisionCode() {
        return stateSubdivisionCode;
    }

    public void setStateSubdivisionCode(String stateSubdivisionCode) {
        this.stateSubdivisionCode = stateSubdivisionCode;
    }
}
