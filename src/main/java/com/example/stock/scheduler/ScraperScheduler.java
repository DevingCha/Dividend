package com.example.stock.scheduler;

import com.example.stock.model.Company;
import com.example.stock.model.ScrapedResult;
import com.example.stock.model.constant.CacheKey;
import com.example.stock.persist.entity.CompanyEntity;
import com.example.stock.persist.entity.DividendEntity;
import com.example.stock.persist.repository.CompanyRepository;
import com.example.stock.persist.repository.DividendRepository;
import com.example.stock.scraper.Scraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScraperScheduler {
    private final CompanyRepository companyRepository;
    private final Scraper yahooFinanceScrapper;
    private final DividendRepository dividendRepository;

    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true) // redis cache에 finance에 해당하는 키는 모두 다 비운다.
    @Scheduled(cron = "${scheduler.scrap.yahoo}") // 매일 정각
    public void yahooFinanceScheduling() {
        log.info("Scraping Scheduler Is Started: " + System.currentTimeMillis());
        List<CompanyEntity> companies = companyRepository.findAll();
        for (CompanyEntity companyEntity : companies) {
//            log.info("Scraping Scheduler Is Started: " + companyEntity.getName() + ": " + System.currentTimeMillis());

//            ScrapedResult scrapedResult = this.yahooFinanceScrapper.scrap(new Company(companyEntity.getTicker(), companyEntity.getName()));

            ScrapedResult scrapedResult = this.yahooFinanceScrapper.scrap(Company.builder()
                    .name(companyEntity.getName())
                    .ticker(companyEntity.getTicker())
                    .build());

            scrapedResult.getDividendEntities().stream()
                    .map(e -> new DividendEntity(companyEntity.getId(), e))
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });
            // thread sleep: 서버에 과부하가 나지 않게
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }
        // saveAll 은 unique Key 같은 것 때문에 중간에 중복키가 발생하게 되면 아에 모두 중지되버림
    }
}
