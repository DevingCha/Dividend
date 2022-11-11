package com.example.stock.persist.entity;

import com.example.stock.model.Company;
import lombok.*;

import javax.persistence.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "Company")
public class CompanyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String ticker;

    private String name;

    public static CompanyEntity of(Company company) {
        return CompanyEntity.builder()
                .ticker(company.getTicker())
                .name(company.getName())
                .build();
    }
}
