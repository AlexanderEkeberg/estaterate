package no.school.mock;

import no.school.dto.NsrUnitDTO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class MockSchoolData {

    private static final String FILE = "mock-schools.json";

    private record MockFile(List<MockSchool> schools) {
    }

    private final List<NsrUnitDTO> schools;

    public MockSchoolData() {
        MockFile file;
        try (InputStream in = new ClassPathResource(FILE).getInputStream()) {
            file = JsonMapper.builder().build().readValue(in, MockFile.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + FILE, e);
        }
        if (file.schools() == null || file.schools().isEmpty()) {
            throw new IllegalStateException(FILE + " has no schools");
        }
        this.schools = file.schools().stream().map(MockSchoolData::toUnit).toList();
    }

    public List<NsrUnitDTO> schools() {
        return schools;
    }

    private static NsrUnitDTO toUnit(MockSchool school) {
        NsrUnitDTO.Koordinat coordinate = new NsrUnitDTO.Koordinat();
        coordinate.setLatitude(school.latitude());
        coordinate.setLongitude(school.longitude());

        NsrUnitDTO unit = new NsrUnitDTO();
        unit.setName(school.name());
        unit.setCoordinate(coordinate);
        return unit;
    }

}
