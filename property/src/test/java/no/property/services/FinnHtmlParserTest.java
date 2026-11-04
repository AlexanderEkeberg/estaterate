package no.property.services;

import no.property.dto.PropertyLocationDTO;
import no.property.exception.ScrapingFailedException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FinnHtmlParserTest {

    static final String URL = "https://www.finn.no/realestate/homes/ad.html?finnkode=1";

    static final String WITH_COORDINATES = """
            <a href="/realestate/homes/search.html?location=1.20061.20507">Grünerløkka</a>
            <script>streamController.enqueue("[\\"postalCode\\",\\"0550\\",\\"lat\\",59.9225,\\"lng\\",10.7590,\\"municipalityNumber\\",301]")</script>
            """;

    static final String WITHOUT_COORDINATES = """
            <script>streamController.enqueue("[\\"postalCode\\",\\"0550\\",\\"municipalityNumber\\",301]")</script>
            """;

    private final FinnHtmlParser parser = new FinnHtmlParser();

    @Test
    void parsesCoordinatesPostalCodeMunicipalityAndDistrict() {
        PropertyLocationDTO result = parser.parse(WITH_COORDINATES, URL);

        assertThat(result.getUrl()).isEqualTo(URL);
        assertThat(result.getPostalCode()).isEqualTo("0550");
        assertThat(result.getLatitude()).isEqualTo(59.9225);
        assertThat(result.getLongitude()).isEqualTo(10.7590);
        assertThat(result.getMunicipalityNumber()).isEqualTo("0301");
        assertThat(result.getDistrict()).isEqualTo("Grünerløkka");
    }

    @Test
    void returnsNullCoordinatesWhenListingHasNone() {
        PropertyLocationDTO result = parser.parse(WITHOUT_COORDINATES, URL);

        assertThat(result.getLatitude()).isNull();
        assertThat(result.getLongitude()).isNull();
        assertThat(result.getPostalCode()).isEqualTo("0550");
    }

    @Test
    void returnsNullForBothWhenOnlyOneCoordinateIsPresent() {
        String html = "<script>streamController.enqueue(\"[\\\"lat\\\",59.9225]\")</script>";

        PropertyLocationDTO result = parser.parse(html, URL);

        assertThat(result.getLatitude()).isNull();
        assertThat(result.getLongitude()).isNull();
    }

    @Test
    void throwsWhenDataBlockIsMissing() {
        assertThatThrownBy(() -> parser.parse("<html>not a listing</html>", URL))
                .isInstanceOf(ScrapingFailedException.class);
    }
}
