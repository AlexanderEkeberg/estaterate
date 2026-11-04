package no.property.mock;

import no.property.dto.PropertyLocationDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockPropertyDataTest {

    @Test
    void loadsCompleteOsloPropertyWithSubmittedUrl() {
        PropertyLocationDTO property = new MockPropertyData().property("https://www.finn.no/realestate/homes/ad.html?finnkode=1");

        assertThat(property.hasRequiredFields()).isTrue();
        assertThat(property.getMunicipalityNumber()).isEqualTo("0301");
        assertThat(property.getDistrict()).isEqualTo("Grünerløkka");
        assertThat(property.getUrl()).isEqualTo("https://www.finn.no/realestate/homes/ad.html?finnkode=1");
    }
}
