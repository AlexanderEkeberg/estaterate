package no.property.services;

import no.property.dto.PropertyLocationDTO;
import no.property.exception.ScrapingFailedException;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
class FinnHtmlParser {


    private static final String DATA_MARKER = "streamController.enqueue(";
    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("\"postalCode\",\"(\\d{4})\"");
    private static final Pattern LAT_PATTERN = Pattern.compile("\"lat\",([\\d.]+)");
    private static final Pattern LNG_PATTERN = Pattern.compile("\"lng\",([\\d.]+)");

    PropertyLocationDTO parse(String html, String url) {
        int markerIndex = html.indexOf(DATA_MARKER);
        if (markerIndex == -1) {
            throw new ScrapingFailedException("Expected data block not found: " + url);
        }

        String section = html.substring(markerIndex).replace("\\\"", "\"");

        String postalCode = extract(POSTAL_CODE_PATTERN, section);
        String latitude = extract(LAT_PATTERN, section);
        String longitude = extract(LNG_PATTERN, section);

        if (latitude == null || longitude == null) {
            throw new ScrapingFailedException("Coordinates not found in listing: " + url);
        }

        return new PropertyLocationDTO(url, postalCode, latitude, longitude);
    }

    private String extract(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }
}
