package no.property.services;

import no.property.client.FinnClient;
import no.property.dto.ScrapeResponseDTO;
import no.property.validation.FinnUrlValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.restclient.RestTemplateBuilder;

class FinnScraperServiceImplTest {
    @Test
    void scrapeFinntPostalCodeCoordsTest() {
        FinnClient finnClient = new FinnClient(new RestTemplateBuilder());
        FinnHtmlParser finnHtmlParser = new FinnHtmlParser();
        FinnScraperServiceImpl service = new FinnScraperServiceImpl(finnClient, finnHtmlParser, new FinnUrlValidator());

        String url = "https://www.finn.no/realestate/homes/ad.html?finnkode=473655500";
        ScrapeResponseDTO result = service.scrape(url);

        System.out.println(result);

    }
}
