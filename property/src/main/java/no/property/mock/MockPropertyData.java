package no.property.mock;

import no.property.dto.PropertyLocationDTO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;

@Component
public class MockPropertyData {

    private static final String FILE = "mock-property.json";

    private final PropertyLocationDTO data;

    public MockPropertyData() {
        try (InputStream file = new ClassPathResource(FILE).getInputStream()) {
            this.data = JsonMapper.builder().build().readValue(file, PropertyLocationDTO.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + FILE, e);
        }
        if (!data.hasRequiredFields()) {
            throw new IllegalStateException(FILE + " is missing required fields");
        }
    }
    public PropertyLocationDTO property(String url) {
        return new PropertyLocationDTO(url, data.getPostalCode(), data.getLatitude(), data.getLongitude(),
                data.getMunicipalityNumber(), data.getDistrict());
    }
}
