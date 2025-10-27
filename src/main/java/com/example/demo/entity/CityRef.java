package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "city_ref") // table name in DB
@Data
public class CityRef {

	  @Id
	@Column(name = "city")
	    private String city;
	  // city name
    @Column(name = "subdivision_code")
    private String subdivisionCode;
    
    @Column(name = "province_code")
    private String ProvinceCode;
    
    @Column(name = "country")
    private String country;
    
    @Column(name = "province")
    private String Province;
  
}
