package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "companies")
public class Company {

    @Id
    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "description")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

}


























