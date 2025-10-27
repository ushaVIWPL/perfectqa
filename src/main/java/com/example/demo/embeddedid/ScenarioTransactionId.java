package com.example.demo.embeddedid;


import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@Embeddable
public class ScenarioTransactionId implements Serializable {

    @Column(name = "business_scenario")
    private String businessScenario;

    @Column(name = "transaction_suffix")
    private String transactionSuffix;
}
