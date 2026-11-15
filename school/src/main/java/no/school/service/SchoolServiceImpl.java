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

    @Override
    public ScoreEvent calculate(PropertySubmittedEvent event) {
        String analysisId = event.analysisId();
        if (event.latitude() == null || event.longitude() == null) {
            log.warn("Property has no coordinates: {}", analysisId);
            return ScoreEvent.failed(analysisId, CATEGORY, "Mangler koordinater");
        }
        if (!OSLO.equals(event.municipalityNumber())) {
            return ScoreEvent.failed(analysisId, CATEGORY, "School støtter foreløpig bare Oslo");
        }

        try {
            double lat = event.latitude();
            double lon = event.longitude();

            List<NsrUnitDTO> schools = fetchSchoolsWithCoordinates();
            if (schools.isEmpty()) {
                return ScoreEvent.failed(analysisId, CATEGORY, "Fikk ikke hentet skoler fra NSR");
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
                    new SchoolData(nearest.getName(), rounded));
        } catch (Exception e) {
            log.error("School failed for {}", analysisId, e);
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
            log.warn("Ingen respons fra NSR");
            return List.of();
        }

        List<NsrUnitDTO> result = new ArrayList<>();
        for (NsrUnitDTO unit : response.getUnits()) {
            if (!unit.isPrimarySchool() || !unit.isActive()) {
                continue;
            }

            NsrUnitDTO detail = nsrClient.getSchoolDetail(unit.getOrganizationNumber());
            if (detail == null || detail.getCoordinate() == null) {
                log.warn("Mangler koordinat for {}", unit.getName());
                continue;
            }

            Double lat = detail.getCoordinate().getLatitude();
            Double lon = detail.getCoordinate().getLongitude();
            if (lat == null || lon == null || (lat == 0.0 && lon == 0.0)) {
                log.warn("Ugyldig koordinat for {}", unit.getName());
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
