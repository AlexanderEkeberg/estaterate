package no.school.service;

import no.school.client.NsrClient;
import no.school.dto.DataSource;
import no.school.dto.NsrUnitDTO;
import no.school.dto.NsrUnitResponseDTO;
import no.school.dto.SchoolData;
import no.school.events.PropertySubmittedEvent;
import no.school.events.ScoreEvent;
import no.school.events.ScoreStatus;
import no.school.mock.MockSchoolData;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


class SchoolServiceTest {
    private final NsrClient nsrClient = mock(NsrClient.class);
    private final MockSchoolData mock = mock(MockSchoolData.class);

    private static PropertySubmittedEvent event(String analysisId, Double lat, Double lon, String municipality) {
        return new PropertySubmittedEvent(null, null, 1, null, analysisId, null, null, lat, lon, municipality, null, "MOCK");
    }

    private static NsrUnitDTO unit(String orgNr, String name, double lat, double lon) {
        NsrUnitDTO.Koordinat coordinate = new NsrUnitDTO.Koordinat();
        coordinate.setLatitude(lat);
        coordinate.setLongitude(lon);

        NsrUnitDTO unit = new NsrUnitDTO();
        unit.setOrganizationNumber(orgNr);
        unit.setName(name);
        unit.setPrimarySchool(true);
        unit.setActive(true);
        unit.setCoordinate(coordinate);
        return unit;


    }
    private SchoolServiceImpl service(boolean fallbackEnabled, boolean mockForced) {
        when(mock.schools()).thenReturn(List.of(unit(null, "Mock school", 59.922, 10.76)));
        SchoolServiceImpl service = new SchoolServiceImpl(nsrClient, mock);
        ReflectionTestUtils.setField(service, "fallbackEnabled", fallbackEnabled);
        ReflectionTestUtils.setField(service, "mockForced", mockForced);
        return service;
    }

    private void nsrReturns(NsrUnitDTO... units) {
        NsrUnitResponseDTO response = new NsrUnitResponseDTO();
        response.setUnits(List.of(units));
        when(nsrClient.getSchoolsByMunicipality("0301")).thenReturn(response);
        for (NsrUnitDTO unit : units) {
            when(nsrClient.getSchoolDetail(unit.getOrganizationNumber())).thenReturn(unit);
        }
    }

    @Test
    void scoreDropsTenPointsPerStartedStep() {
        assertEquals(100, SchoolServiceImpl.scoreFor(0.25));
        assertEquals(90, SchoolServiceImpl.scoreFor(0.26));
        assertEquals(90, SchoolServiceImpl.scoreFor(0.5));
        assertEquals(80, SchoolServiceImpl.scoreFor(0.51));
        assertEquals(10, SchoolServiceImpl.scoreFor(2.5));
        assertEquals(0, SchoolServiceImpl.scoreFor(2.51));
    }

    @Test
    void missingCoordinatesGivesFailedWithoutCallingNsr() {
        ScoreEvent result = service(true, false).calculate(event("a1", null, null, "0301"));

        assertEquals(ScoreStatus.FAILED, result.status());
        assertEquals("SCHOOL", result.category());
        verifyNoInteractions(nsrClient);
    }

    @Test
    void propertyOutsideOsloGivesFailedWithoutCallingNsr() {
        ScoreEvent result = service(true, false).calculate(event("a1", 59.92, 10.76, "3024"));

        assertEquals(ScoreStatus.FAILED, result.status());
        verifyNoInteractions(nsrClient);
    }

    @Test
    void unexpectedExceptionGivesFailedInsteadOfThrowing() {
        when(nsrClient.getSchoolsByMunicipality(anyString())).thenThrow(new IllegalStateException("oops"));

        ScoreEvent result = service(true, false).calculate(event("a1", 59.92, 10.76, "0301"));

        assertEquals(ScoreStatus.FAILED, result.status());
        assertEquals("oops", result.error());
    }

    @Test
    void eachEventKeepsItsOwnAnalysisId() {
        SchoolServiceImpl service = service(true, false);

        assertEquals("first", service.calculate(event("first", 59.92, 10.76, "0301")).analysisId());
        assertEquals("second", service.calculate(event("second", 59.92, 10.76, "0301")).analysisId());
    }

    @Test
    void nearestSchoolIsChosenAndMarkedLive() {
        nsrReturns(unit("A", "School A", 59.922, 10.76), unit("B", "School B", 59.95, 10.76));

        ScoreEvent result = service(true, false).calculate(event("a1", 59.92, 10.76, "0301"));
        SchoolData data = (SchoolData) result.data();

        assertEquals(ScoreStatus.SUCCESS, result.status());
        assertEquals(100, result.score());
        assertEquals("School A", data.nearestSchool());
        assertEquals(0.22, data.distanceKm(), 0.01);
        assertEquals(DataSource.LIVE, data.dataSource());
    }

    @Test
    void usesMockWhenNsrFails() {
        ScoreEvent result = service(true, false).calculate(event("a1", 59.92, 10.76, "0301"));
        SchoolData data = (SchoolData) result.data();

        assertEquals(ScoreStatus.SUCCESS, result.status());
        assertEquals("Mock school", data.nearestSchool());
        assertEquals(DataSource.MOCK, data.dataSource());
    }

    @Test
    void failsWhenNsrFailsAndFallbackIsOff() {
        ScoreEvent result = service(false, false).calculate(event("a1", 59.92, 10.76, "0301"));

        assertEquals(ScoreStatus.FAILED, result.status());
        assertNull(result.score());
        assertNull(result.data());
    }

    @Test
    void forcedMockSkipsNsr() {
        nsrReturns(unit("A", "School A", 59.922, 10.76));

        ScoreEvent result = service(false, true).calculate(event("a1", 59.92, 10.76, "0301"));

        assertEquals(DataSource.MOCK, ((SchoolData) result.data()).dataSource());
        verify(nsrClient, never()).getSchoolsByMunicipality(anyString());
    }

}
