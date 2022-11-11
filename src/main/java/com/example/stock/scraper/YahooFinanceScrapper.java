package com.example.stock.scraper;

import com.example.stock.model.Company;
import com.example.stock.model.Dividend;
import com.example.stock.model.ScrapedResult;
import com.example.stock.model.constant.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScrapper implements Scraper {
    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";
    private static final long START_TIME = 86400;

    @Override
    public ScrapedResult scrap(Company company) {
        var scrapedResult = new ScrapedResult();
        scrapedResult.setCompany(company);

        try {
            long start = 0;
            long end = System.currentTimeMillis() / 1000;
            Connection connection =
                    Jsoup.connect(String.format(STATISTICS_URL, company.getTicker(), start, end));

            Document document = connection.get();
            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tableElement = parsingDivs.get(0);


            Element tbody = tableElement.children().get(1);
            List<Dividend> dividens = new ArrayList<>();

            for (Element tr : tbody.children()) {
                String txt = tr.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                if (month < 0 || month > 12) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }

                int date = Integer.parseInt(splits[1].replace(",", ""));
                int year = Integer.parseInt(splits[2]);
                String dividend = splits[3];

                dividens.add(
                        Dividend.builder()
                                .date(LocalDateTime.of(year, month, date, 0, 0)).dividend(dividend)
                                .build());
//                dividens.add(new Dividend(LocalDateTime.of(year, month, date, 0, 0), dividend));

                scrapedResult.setDividendEntities(dividens);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return scrapedResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {
        String url = String.format(SUMMARY_URL, ticker, ticker);
        try {
            Document document = Jsoup.connect(url).get();
            Element titleElement = document.getElementsByTag("h1").get(0);
            String title = titleElement.text().split("\\(")[0].trim();

            return Company.builder().ticker(ticker).name(title).build();
//            return new Company(ticker, title);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
