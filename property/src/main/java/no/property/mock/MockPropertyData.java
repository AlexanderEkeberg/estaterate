package no.property.mock;

import no.property.dto.PropertyLocationDTO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MockPropertyData {

    private static final String FILE = "mock-property.json";

    private record MockFile(MockListing fallback, List<MockListing> listings) {
    }

    private final MockListing fallback;
    private final Map<String, MockListing> listingsByFinnkode;

    public MockPropertyData() {
        MockFile file;
        try (InputStream in = new ClassPathResource(FILE).getInputStream()) {
            file = JsonMapper.builder().build().readValue(in, MockFile.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + FILE, e);
        }
        this.fallback = file.fallback();
        this.listingsByFinnkode = file.listings().stream()
                .collect(Collectors.toMap(MockListing::finnkode, Function.identity()));

        if (!toDto(fallback, null).hasRequiredFields()) {
            throw new IllegalStateException(FILE + ": fallback is missing required fields");
        }
        listingsByFinnkode.values().forEach(listing -> {
            if (!toDto(listing, null).hasRequiredFields()) {
                throw new IllegalStateException(FILE + ": listing " + listing.finnkode() + " is missing required fields");
            }
        });
    }

    public PropertyLocationDTO fallback(String url) {
        return toDto(fallback, url);
    }

    public Optional<PropertyLocationDTO> demoListing(URI uri) {
        return finnkode(uri)
                .map(listingsByFinnkode::get)
                .map(listing -> toDto(listing, uri.toString()));
    }

    private static Optional<String> finnkode(URI uri) {
        String query = uri.getRawQuery();
        if (query == null) {
            return Optional.empty();
        }
        for (String param : query.split("&")) {
            if (param.startsWith("finnkode=")) {
                return Optional.of(param.substring("finnkode=".length()));
            }
        }
        return Optional.empty();
    }

    private static PropertyLocationDTO toDto(MockListing listing, String url) {
        return new PropertyLocationDTO(url, listing.postalCode(), listing.latitude(), listing.longitude(),
                listing.municipalityNumber(), listing.district());
    }
}
