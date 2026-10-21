package org.example.crimerate.service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum OsloBydel {
    GAMLE_OSLO("1", "Gamle Oslo", "Gamle Oslo", "Helsfyr - Sinsen"),
    GRUNERLOKKA("2", "Grünerløkka", "Grünerløkka - Sofienberg"),
    SAGENE("3", "Sagene", "Sagene - Torshov"),
    ST_HANSHAUGEN("4", "St. Hanshaugen", "St.Hanshaugen - Ullevål"),
    FROGNER("5", "Frogner", "Bygdøy - Frogner", "Uranienborg - Majorstuen"),
    ULLERN("6", "Ullern", "Ullern"),
    VESTRE_AKER("7", "Vestre Aker", "Vinderen", "Røa"),
    NORDRE_AKER("8", "Nordre Aker", "Grefsen - Kjelsås", "Sogn"),
    BJERKE("9", "Bjerke", "Bjerke"),
    GRORUD("10", "Grorud", "Grorud", "Romsås"),
    STOVNER("11", "Stovner", "Stovner"),
    ALNA("12", "Alna", "Furuset", "Hellerud"),
    OSTENSJO("13", "Østensjø", "Østensjø", "Bøler", "Manglerud"),
    NORDSTRAND("14", "Nordstrand", "Nordstrand", "Lambertseter", "Ekeberg - Bekkelaget"),
    SONDRE_NORDSTRAND("15", "Søndre Nordstrand", "Søndre Nordstrand");

    private final String code;
    private final String displayName;
    private final List<String> finnAreas;

    OsloBydel(String code, String displayName, String... finnAreas) {
        this.code = code;
        this.displayName = displayName;
        this.finnAreas = List.of(finnAreas);
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<OsloBydel> fromFinnDistrict(String district) {
        if (district == null) {
            return Optional.empty();
        }
        String normalized = normalize(district);
        return Arrays.stream(values())
                .filter(bydel -> bydel.finnAreas.stream()
                        .anyMatch(area -> normalize(area).equals(normalized)))
                .findFirst();
    }

    private static String normalize(String name) {
        return name.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}
