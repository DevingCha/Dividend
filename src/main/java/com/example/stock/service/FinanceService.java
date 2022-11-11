package com.example.stock.service;

import com.example.stock.exception.impl.NoCompanyException;
import com.example.stock.model.Company;
import com.example.stock.model.Dividend;
import com.example.stock.model.ScrapedResult;
import com.example.stock.model.constant.CacheKey;
import com.example.stock.persist.entity.CompanyEntity;
import com.example.stock.persist.entity.DividendEntity;
import com.example.stock.persist.repository.CompanyRepository;
import com.example.stock.persist.repository.DividendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class FinanceService {
    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);
        CompanyEntity companyEntity = this.companyRepository.findByName(companyName)
                .orElseThrow(() -> new NoCompanyException());


        List<DividendEntity> dividendEntityList =
                dividendRepository.findAllByCompanyId(companyEntity.getId());

        List<Dividend> dividendList = dividendEntityList.stream()
                .map(Dividend::fromEntity).collect(Collectors.toList());

        return ScrapedResult.builder()
                .company(Company.fromEntity(companyEntity))
                .dividendEntities(dividendList)
                .build();

//        List<Dividend> dividendList = dividendEntityList.stream()
//                .map(e -> new Dividend(e.getDate(), e.getDividend()))
//                        .collect(Collectors.toList());

//        return ScrapedResult.builder()
//                .company(new Company(companyEntity.getTicker(), companyEntity.getName()))
//                .dividendEntities(dividendList)
//                .build();

    }
}
