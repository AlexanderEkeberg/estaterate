package no.property.services;

import no.property.client.FinnClient;
import no.property.dto.PropertyLocationDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.restclient.RestTemplateBuilder;

class FinnScraperServiceImplTest {
    @Test
    void scrapeFinntPostalCodeCoordsTest() {
        FinnClient finnClient = new FinnClient(new RestTemplateBuilder());
        FinnHtmlParser finnHtmlParser = new FinnHtmlParser();
        // La til null her hvor KafkaProduceren egentlig ligger - mulig det må fikses senere
        FinnScraperServiceImpl service = new FinnScraperServiceImpl(finnClient, finnHtmlParser, null);

        String url = "https://www.finn.no/realestate/homes/ad.html?finnkode=473655500";
        PropertyLocationDTO result = service.scrape(url);

        System.out.println(result);

    }
}
