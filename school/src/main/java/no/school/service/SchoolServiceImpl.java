package no.school.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.school.dto.NsrUnitDTO;
import no.school.dto.NsrUnitResponseDTO;
import no.school.events.ScoreEvent;
import org.springframework.stereotype.Service;
import no.school.client.NsrClient;
import no.school.dto.SchoolData;
import no.school.events.PropertySubmittedEvent;
import no.school.dto.DataSource;
import no.school.mock.MockSchoolData;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private static final String CATEGORY = "SCHOOL";
    private static final String OSLO = "0301";
    private final NsrClient nsrClient;
    private List<NsrUnitDTO> cachedSchools;
    private final MockSchoolData mock;

    @Value("${school.mock.fallback-enabled:false}")
    private boolean fallbackEnabled;

    @Value("${school.mock.force:false}")
    private boolean mockForced;

    @Override
    public ScoreEvent calculate(PropertySubmittedEvent event) {
        String analysisId = event.analysisId();
        if (event.latitude() == null || event.longitude() == null) {
            log.atWarn()
                    .addKeyValue("event_type", "coordinates_missing")
                    .addKeyValue("analysis_id", analysisId)
                    .log("Property has no coordinates");
            return ScoreEvent.failed(analysisId, CATEGORY, "Missing coordinates");
        }
        if (!OSLO.equals(event.municipalityNumber())) {
            return ScoreEvent.failed(analysisId, CATEGORY, "School currently supports Oslo only");
        }

        try {
            double lat = event.latitude();
            double lon = event.longitude();

            DataSource source = DataSource.LIVE;
            List<NsrUnitDTO> schools = mockForced ? List.of() : fetchSchoolsWithCoordinates();
            if (schools.isEmpty()) {
                if (!mockForced && !fallbackEnabled) {
                return ScoreEvent.failed(analysisId, CATEGORY, "Could not fetch schools from NSR");
                }
                log.atWarn()
                        .addKeyValue("event_type", "mock_fallback_used")
                        .addKeyValue("analysis_id", analysisId)
                        .addKeyValue("reason", mockForced ? "Mock forced by config" : "No schools from NSR")
                        .log("Using mock schools");
                schools = mock.schools();
                source = DataSource.MOCK;
            }

            NsrUnitDTO nearest = null;
            double minDistance = Double.MAX_VALUE;
            for (NsrUnitDTO school : schools) {
                double distance = distanceKm(lat, lon,
                        school.getCoordinate().getLatitude(),
                        school.getCoordinate().getLongitude());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = school;
                }
            }

            int score = scoreFor(minDistance);
            double rounded = Math.round(minDistance * 100.0) / 100.0;

            return ScoreEvent.success(analysisId, CATEGORY, score,
                    new SchoolData(nearest.getName(), rounded, source));
        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("event_type", "score_calculation_failed")
                    .addKeyValue("analysis_id", analysisId)
                    .log("School calculation failed");
            String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ScoreEvent.failed(analysisId, CATEGORY, message);
        }
    }


    private synchronized List<NsrUnitDTO> fetchSchoolsWithCoordinates() {
        if (cachedSchools != null) {
            return cachedSchools;
        }

        NsrUnitResponseDTO response = nsrClient.getSchoolsByMunicipality(OSLO);
        if (response == null || response.getUnits() == null) {
            log.atWarn()
                    .addKeyValue("event_type", "nsr_no_data")
                    .log("No response from NSR");
            return List.of();
        }

        List<NsrUnitDTO> result = new ArrayList<>();
        for (NsrUnitDTO unit : response.getUnits()) {
            if (!unit.isPrimarySchool() || !unit.isActive()) {
                continue;
            }

            NsrUnitDTO detail = nsrClient.getSchoolDetail(unit.getOrganizationNumber());
            if (detail == null || detail.getCoordinate() == null) {
                log.atWarn()
                        .addKeyValue("event_type", "school_coordinates_missing")
                        .addKeyValue("school", unit.getName())
                        .log("School has no coordinates");
                continue;
            }

            Double lat = detail.getCoordinate().getLatitude();
            Double lon = detail.getCoordinate().getLongitude();
            if (lat == null || lon == null || (lat == 0.0 && lon == 0.0)) {
                log.atWarn()
                        .addKeyValue("event_type", "school_coordinates_invalid")
                        .addKeyValue("school", unit.getName())
                        .log("School has invalid coordinates");
                continue;
            }

            result.add(detail);
        }

        if (!result.isEmpty()) {
            cachedSchools = result;
        }
        return result;
    }

    static int scoreFor(double distanceKm) {
        int steps = (int) Math.ceil(Math.max(distanceKm, 0.25) / 0.25) - 1;
        return Math.max(0, 100 - 10 * steps);
    }

    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        double y = Math.toRadians(lat2 - lat1);
        return 6371.0 * Math.sqrt(x * x + y * y);
    }
}
