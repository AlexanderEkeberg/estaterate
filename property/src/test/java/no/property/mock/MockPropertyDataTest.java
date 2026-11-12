package no.property.mock;

import no.property.dto.PropertyDetailsDTO;
import no.property.dto.PropertyLocationDTO;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class MockPropertyDataTest {

    private final MockPropertyData mock = new MockPropertyData();

    @Test
    void fallbackIsCompleteOsloPropertyWithSubmittedUrl() {
        PropertyLocationDTO property = mock.fallback("https://www.finn.no/realestate/homes/ad.html?finnkode=1");

        assertThat(property.hasRequiredFields()).isTrue();
        assertThat(property.getMunicipalityNumber()).isEqualTo("0301");
        assertThat(property.getDistrict()).isEqualTo("Grünerløkka - Sofienberg");
        assertThat(property.getUrl()).isEqualTo("https://www.finn.no/realestate/homes/ad.html?finnkode=1");
    }

    @Test
    void findsDemoListingByFinnkode() {
        URI uri = URI.create("https://www.finn.no/realestate/homes/ad.html?finnkode=478564477");

        PropertyLocationDTO property = mock.demoListing(uri).orElseThrow();

        assertThat(property.getPostalCode()).isEqualTo("0272");
        assertThat(property.getDistrict()).isEqualTo("Bygdøy - Frogner");
        assertThat(property.getUrl()).isEqualTo(uri.toString());
    }

    @Test
    void findsDemoListingWhenUrlHasExtraParameters() {
        URI uri = URI.create("https://www.finn.no/realestate/homes/ad.html?utm_source=x&finnkode=476403696&ref=y");

        assertThat(mock.demoListing(uri)).isPresent();
    }

    @Test
    void unknownFinnkodeOrMissingFinnkodeGivesNoDemoListing() {
        assertThat(mock.demoListing(URI.create("https://www.finn.no/realestate/homes/ad.html?finnkode=1"))).isEmpty();
        assertThat(mock.demoListing(URI.create("https://www.finn.no/realestate/homes/ad.html"))).isEmpty();
    }

    @Test
    void detailsForDemoListingMatchTheSavedListing() {
        URI uri = URI.create("https://www.finn.no/realestate/homes/ad.html?finnkode=478564477");

        PropertyDetailsDTO details = mock.details(uri).orElseThrow();

        assertThat(details).isEqualTo(
                new PropertyDetailsDTO("Niels Juels gate 7, 0272 Oslo", "0272", 17_800_000, 179));
    }

    @Test
    void unknownOrMissingFinnkodeGivesNoDetails() {
        assertThat(mock.details(URI.create("https://www.finn.no/realestate/homes/ad.html?finnkode=1"))).isEmpty();
        assertThat(mock.details(URI.create("https://www.finn.no/realestate/homes/ad.html"))).isEmpty();
    }
}
