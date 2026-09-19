package com.example.demo.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import jakarta.persistence.Lob;

@Entity
@Table(name = "company_ref")
public class CompanyRef {

    @Id
    @Column(name = "company_code")
    private String companyCode;
    
    @Column(name = "company_name")
    private String companyName;
    
    @Column(name = "company_address")
    private String companyAddress;
    
    @Column(name = "country")
    private String country;
    
    @Column(name = "state")
    private String state;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "zip")
    private String zip;

    @Column(name = "logo_path")
    private String logoPath;

    @Lob
    @Column(name = "logo_data", columnDefinition = "LONGBLOB")
    private byte[] logoData;

    @Column(name = "logo_content_type")
    private String logoContentType;

    @OneToMany(mappedBy = "company", cascade = CascadeType.PERSIST)
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

    public String getLogoPath() {
        return logoPath;
    }
    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public byte[] getLogoData() {
        return logoData;
    }
    public void setLogoData(byte[] logoData) {
        this.logoData = logoData;
    }

    public String getLogoContentType() {
        return logoContentType;
    }
    public void setLogoContentType(String logoContentType) {
        this.logoContentType = logoContentType;
    }
    
    @Override
    public String toString() {
        return "CompanyRef{" +
                "companyCode='" + companyCode + '\'' +
                ", companyName='" + companyName + '\'' +
                '}';
    }

}
