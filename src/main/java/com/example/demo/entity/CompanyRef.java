package com.example.demo.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class CompanyRef {

    @Id
    private String companyCode;
    private String companyName;
    private String companyAddress;
    private String country;
    private String state;
    private String city;
    private String zip;

    
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<ProjectRef> projects;
    
    // Getters and Setters
    public String getCompanyCode() { 
        return companyCode; 
    }
    public void setCompanyCode(String companyCode) { 
        this.companyCode = companyCode; 
    }

    public String getCompanyName() { 
        return companyName; 
    }
    public void setCompanyName(String companyName) { 
        this.companyName = companyName; 
    }

    public String getCompanyAddress() { 
        return companyAddress; 
    }
    public void setCompanyAddress(String companyAddress) { 
        this.companyAddress = companyAddress; 
    }

    public String getCountry() { 
        return country; 
    }
    public void setCountry(String country) { 
        this.country = country; 
    }

    public String getState() { 
        return state; 
    }
    public void setState(String state) { 
        this.state = state; 
    }

    public String getCity() { 
        return city; 
    }
    public void setCity(String city) { 
        this.city = city; 
    }

    public String getZip() { 
        return zip; 
    }
    public void setZip(String zip) { 
        this.zip = zip; 
    }
}
