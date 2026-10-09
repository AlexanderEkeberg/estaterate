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
    private static final Pattern MUNICIPALITY_PATTERN = Pattern.compile("\"municipalityNumber\",(\\d+)");
    private static final Pattern LOCATION_LINK_PATTERN = Pattern.compile(
            "<a href=\"[^\"]*location=([\\d.]+)\"[^>]*>([^<]+)</a>");

    PropertyLocationDTO parse(String html, String url) {
        int markerIndex = html.indexOf(DATA_MARKER);
        if (markerIndex == -1) {
            throw new ScrapingFailedException("Expected data block not found: " + url);
        }

        String section = html.substring(markerIndex).replace("\\\"", "\"");

        String postalCode = extract(POSTAL_CODE_PATTERN, section);
        String rawLatitude = extract(LAT_PATTERN, section);
        String rawLongitude = extract(LNG_PATTERN, section);

        if (rawLatitude == null || rawLongitude == null) {
            throw new ScrapingFailedException("Coordinates not found in listing: " + url);
        }

        double latitude;
        double longitude;
        try {
            latitude = Double.parseDouble(rawLatitude);
            longitude = Double.parseDouble(rawLongitude);
        } catch (NumberFormatException e) {
            throw new ScrapingFailedException("Invalid coordinates in listing: " + url, e);
        }

        String rawMunicipality = extract(MUNICIPALITY_PATTERN, section);
        String municipalityNumber = rawMunicipality == null
                ? null
                : String.format("%04d", Integer.parseInt(rawMunicipality));
        String district = extractDistrict(html);

        return new PropertyLocationDTO(url, postalCode, latitude, longitude, municipalityNumber, district);
    }

    private String extractDistrict(String html) {
        String district = null;
        Matcher matcher = LOCATION_LINK_PATTERN.matcher(html);
        while (matcher.find()) {
            String locationId = matcher.group(1);
            if (locationId.startsWith("2.") || locationId.startsWith("1.20061.")) {
                district = matcher.group(2).trim();
            }
        }
        return district;
    }

    private String extract(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }
}
