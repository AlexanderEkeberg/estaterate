package org.example.crimerate.mock;

import org.example.crimerate.client.OsloStatbankClient;
import org.example.crimerate.service.OsloBydel;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class MockCrimeDataTest {

    private final MockCrimeData mock = new MockCrimeData();

    @Test
    void isForTheLatestYear() {
        assertThat(mock.year()).isEqualTo(OsloStatbankClient.LAST_YEAR);
    }

    @Test
    void hasCrimeCountsAndPopulationForEveryBydel() {
        String[] codes = Arrays.stream(OsloBydel.values()).map(OsloBydel::getCode).toArray(String[]::new);

        assertThat(mock.crimeCounts()).containsOnlyKeys(codes);
        assertThat(mock.population()).containsOnlyKeys(codes);
        assertThat(mock.population().values()).allMatch(people -> people > 0);
    }
}
