package no.school.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.school.client.PropertyClient;
import no.school.dto.NsrUnitDTO;
import no.school.dto.NsrUnitResponseDTO;
import no.school.dto.PropertyLocationDTO;
import no.school.dto.SchoolRatingDTO;
import org.springframework.stereotype.Service;
import no.school.client.NsrClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final PropertyClient propertyClient;
    private final NsrClient nsrClient;

    private List<NsrUnitDTO> cachedSchools;

    @Override
    public SchoolRatingDTO getRating() {
        /*
        PropertyLocationDTO location = propertyClient.getPropertyLocation();
        if (location == null || location.getLatitude() == null || location.getLongitude() == null) {
            log.warn("Ingen lokasjonsdata tilgjengelig fra property");
            return null;
        }
        double lat = Double.parseDouble(location.getLatitude());
        double lon = Double.parseDouble(location.getLongitude());
        */
        //Test coordinates
        //double lat = 59.9139;
        //double lon = 10.7522;

        double lat = 61.1151;
        double lon = 10.4663;

        List<NsrUnitDTO> schools = fetchSchoolsWithCoordinates();
        if (schools.isEmpty()) {
            log.warn("Ingen skoler å regne avstand mot");
            return null;
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

        int score = minDistance <= 5.0 ? 100 : minDistance <= 10.0 ? 50 : 0;
        double rounded = Math.round(minDistance * 100.0) / 100.0;

        return new SchoolRatingDTO(score, nearest.getName(), rounded);
    }

    private synchronized List<NsrUnitDTO> fetchSchoolsWithCoordinates() {
        if (cachedSchools != null) {
            return cachedSchools;
        }

        NsrUnitResponseDTO response = nsrClient.getSchoolsByMunicipality("0301");
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


    private double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        double y = Math.toRadians(lat2 - lat1);
        return 6371.0 * Math.sqrt(x * x + y * y);
    }
}
