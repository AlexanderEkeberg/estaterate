package no.property.services;

import no.property.client.FinnClient;
import no.property.dto.PropertyDetailsDTO;
import no.property.dto.ScrapeResponseDTO;
import no.property.event.CoordinatesSource;
import no.property.event.PropertySubmittedEvent;
import no.property.exception.InvalidFinnUrlException;
import no.property.exception.ScrapingFailedException;
import no.property.mock.MockPropertyData;
import no.property.validation.FinnUrlValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.restclient.RestTemplateBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static no.property.services.FinnHtmlParserTest.URL;
import static no.property.services.FinnHtmlParserTest.WITHOUT_COORDINATES;
import static no.property.services.FinnHtmlParserTest.WITH_COORDINATES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FinnScraperServiceImplTest {

    private final List<PropertySubmittedEvent> published = new ArrayList<>();
    private final MockPropertyData mock = new MockPropertyData();
    private int fetchCalls = 0;

    private FinnScraperServiceImpl service(FinnClient finnClient, boolean fallbackEnabled, boolean mockForced) {
        return new FinnScraperServiceImpl(finnClient, new FinnHtmlParser(), new FinnUrlValidator(),
                published::add, mock, fallbackEnabled, mockForced);
    }

    private FinnClient returning(String html) {
        return new FinnClient(new RestTemplateBuilder()) {
            @Override
            public String fetchFinnHtml(URI uri) {
                fetchCalls++;
                return html;
            }
        };
    }

    private FinnClient failing() {
        return new FinnClient(new RestTemplateBuilder()) {
            @Override
            public String fetchFinnHtml(URI uri) {
                fetchCalls++;
                throw new ScrapingFailedException("403 Forbidden");
            }
        };
    }

    private void assertWholeMockProperty(PropertySubmittedEvent event) {
        var expected = mock.fallback(URL);
        assertThat(event.coordinatesSource()).isEqualTo(CoordinatesSource.MOCK);
        assertThat(event.url()).isEqualTo(URL);
        assertThat(event.postalCode()).isEqualTo(expected.getPostalCode());
        assertThat(event.latitude()).isEqualTo(expected.getLatitude());
        assertThat(event.longitude()).isEqualTo(expected.getLongitude());
        assertThat(event.municipalityNumber()).isEqualTo(expected.getMunicipalityNumber());
        assertThat(event.district()).isEqualTo(expected.getDistrict());
    }

    @Test
    void completeListingIsPublishedAsScraped() {
        ScrapeResponseDTO response = service(returning(WITH_COORDINATES), true, false).scrape(URL);

        PropertySubmittedEvent event = published.getFirst();
        assertThat(event.analysisId()).isEqualTo(response.getAnalysisId());
        assertThat(event.coordinatesSource()).isEqualTo(CoordinatesSource.SCRAPED);
        assertThat(event.latitude()).isEqualTo(59.9225);
        assertThat(event.longitude()).isEqualTo(10.7590);
        assertThat(event.postalCode()).isEqualTo("0550");
        assertThat(response.getCoordinatesSource()).isEqualTo(CoordinatesSource.SCRAPED);
        assertThat(response.getMessage()).isNull();
    }

    @Test
    void missingRequiredFieldGivesWholeMockPropertyNotAMix() {
        // annonsen har postnummer 0550, men mangler koordinater, så ingenting fra den skal brukes
        ScrapeResponseDTO response = service(returning(WITHOUT_COORDINATES), true, false).scrape(URL);

        assertWholeMockProperty(published.getFirst());
        assertThat(response.getCoordinatesSource()).isEqualTo(CoordinatesSource.MOCK);
        assertThat(response.getMessage()).isEqualTo(FinnScraperServiceImpl.MOCK_MESSAGE);
    }

    @Test
    void finnFailingGivesWholeMockProperty() {
        ScrapeResponseDTO response = service(failing(), true, false).scrape(URL);

        assertWholeMockProperty(published.getFirst());
        assertThat(response.getCoordinatesSource()).isEqualTo(CoordinatesSource.MOCK);
    }

    @Test
    void forcedMockSkipsFinn() {
        service(returning(WITH_COORDINATES), true, true).scrape(URL);

        assertThat(fetchCalls).isZero();
        assertWholeMockProperty(published.getFirst());
    }

    @Test
    void missingFieldWithFallbackDisabledFailsAndPublishesNothing() {
        assertThatThrownBy(() -> service(returning(WITHOUT_COORDINATES), false, false).scrape(URL))
                .isInstanceOf(ScrapingFailedException.class);
        assertThat(published).isEmpty();
    }

    @Test
    void finnFailingWithFallbackDisabledFailsAndPublishesNothing() {
        assertThatThrownBy(() -> service(failing(), false, false).scrape(URL))
                .isInstanceOf(ScrapingFailedException.class);
        assertThat(published).isEmpty();
    }

    @Test
    void invalidUrlIsRejectedEvenWhenMockIsForced() {
        assertThatThrownBy(() -> service(returning(WITH_COORDINATES), true, true).scrape("https://example.com/"))
                .isInstanceOf(InvalidFinnUrlException.class);
        assertThat(published).isEmpty();
    }

    @Test
    void demoListingIsUsedWithoutAskingFinn() {
        String url = "https://www.finn.no/realestate/homes/ad.html?finnkode=478564477";

        ScrapeResponseDTO response = service(returning(WITH_COORDINATES), true, false).scrape(url);

        PropertySubmittedEvent event = published.getFirst();
        assertThat(fetchCalls).isZero();
        assertThat(event.coordinatesSource()).isEqualTo(CoordinatesSource.MOCK);
        assertThat(event.postalCode()).isEqualTo("0272");
        assertThat(event.district()).isEqualTo("Bygdøy - Frogner");
        assertThat(event.url()).isEqualTo(url);
        assertThat(response.getMessage()).isEqualTo(FinnScraperServiceImpl.DEMO_MESSAGE);
    }

    @Test
    void demoListingIsIgnoredWhenFallbackIsDisabled() {
        service(returning(WITH_COORDINATES), false, false)
                .scrape("https://www.finn.no/realestate/homes/ad.html?finnkode=478564477");

        assertThat(fetchCalls).isEqualTo(1);
        assertThat(published.getFirst().coordinatesSource()).isEqualTo(CoordinatesSource.SCRAPED);
    }

    private static final String FROGNER = "https://www.finn.no/realestate/homes/ad.html?finnkode=478564477";
    private static final String SOFIENBERG = "https://www.finn.no/realestate/homes/ad.html?finnkode=476403696";

    @Test
    void detailsBelongToTheSameListingAsThePublishedEvent() {
        FinnScraperServiceImpl service = service(failing(), true, false);

        service.scrape(FROGNER);
        PropertyDetailsDTO details = service.getDetails(FROGNER).orElseThrow();

        assertThat(published.getFirst().url()).isEqualTo(FROGNER);
        assertThat(details.postalCode()).isEqualTo(published.getFirst().postalCode());
        assertThat(details.address()).isEqualTo("Niels Juels gate 7, 0272 Oslo");
    }

    @Test
    void detailsDoNotDependOnWhichListingWasScrapedLast() {
        FinnScraperServiceImpl service = service(failing(), true, false);

        PropertyDetailsDTO before = service.getDetails(FROGNER).orElseThrow();
        service.scrape(SOFIENBERG);
        PropertyDetailsDTO after = service.getDetails(FROGNER).orElseThrow();

        assertThat(after).isEqualTo(before);
        assertThat(service.getDetails(SOFIENBERG).orElseThrow().address())
                .isEqualTo("Sofienberggata 23B, 0558 Oslo");
    }

    @Test
    void detailsRejectInvalidUrlsWithoutContactingFinn() {
        FinnScraperServiceImpl service = service(failing(), true, false);

        for (String url : List.of(
                "http://www.finn.no/realestate/homes/ad.html?finnkode=478564477",
                "https://evil.example/realestate/homes/ad.html?finnkode=478564477",
                "https://www.finn.no/car/used/ad.html?finnkode=478564477")) {
            assertThatThrownBy(() -> service.getDetails(url)).isInstanceOf(InvalidFinnUrlException.class);
        }
        assertThat(fetchCalls).isZero();
    }

    @Test
    void detailsNeverContactFinn() {
        assertThat(service(failing(), true, false).getDetails(URL)).isEmpty();
        assertThat(service(failing(), false, false).getDetails(FROGNER)).isEmpty();
        assertThat(fetchCalls).isZero();
    }
}
