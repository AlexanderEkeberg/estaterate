package org.example.crimerate.service;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum OsloBydel {
    GAMLE_OSLO("1", "Gamle Oslo"),
    GRUNERLOKKA("2", "Grünerløkka"),
    SAGENE("3", "Sagene"),
    ST_HANSHAUGEN("4", "St. Hanshaugen"),
    FROGNER("5", "Frogner"),
    ULLERN("6", "Ullern"),
    VESTRE_AKER("7", "Vestre Aker"),
    NORDRE_AKER("8", "Nordre Aker"),
    BJERKE("9", "Bjerke"),
    GRORUD("10", "Grorud"),
    STOVNER("11", "Stovner"),
    ALNA("12", "Alna"),
    OSTENSJO("13", "Østensjø"),
    NORDSTRAND("14", "Nordstrand"),
    SONDRE_NORDSTRAND("15", "Søndre Nordstrand");

    private final String code;
    private final String displayName;

    OsloBydel(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<OsloBydel> fromName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        String normalized = normalize(name);
        return Arrays.stream(values())
                .filter(bydel -> normalize(bydel.displayName).equals(normalized))
                .findFirst();
    }

    private static String normalize(String name) {
        return name.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }
}
