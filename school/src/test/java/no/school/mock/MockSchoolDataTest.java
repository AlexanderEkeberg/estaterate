package no.school.mock;

import no.school.dto.NsrUnitDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class MockSchoolDataTest {

    private final List<NsrUnitDTO> schools = new MockSchoolData().schools();

    @Test
    void loadsSchoolsFromFile() {
        assertFalse(schools.isEmpty());
    }

    @Test
    void mapsNameAndCoordinatesFromFile() {
        NsrUnitDTO lakkegata = schools.stream()
                .filter(school -> "Lakkegata skole".equals(school.getName()))
                .findFirst()
                .orElseThrow();

        double lat = lakkegata.getCoordinate().getLatitude();
        double lon = lakkegata.getCoordinate().getLongitude();

        assertEquals(59.91995, lat, 0.00001);
        assertEquals(10.76764, lon, 0.00001);
    }

}
