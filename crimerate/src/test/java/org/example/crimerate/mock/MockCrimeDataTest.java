package org.example.crimerate.mock;

import org.example.crimerate.client.OsloStatbankClient;
import org.example.crimerate.service.OsloBydel;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class MockCrimeDataTest {

    private final MockCrimeData mock = new MockCrimeData();

    @Test
    void coversEveryYearFromFirstToLast() {
        assertThat(mock.years()).containsExactly(2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024);
        assertThat(mock.years().getFirst()).isEqualTo(OsloStatbankClient.FIRST_YEAR);
        assertThat(mock.years().getLast()).isEqualTo(OsloStatbankClient.LAST_YEAR);
    }

    @Test
    void hasCrimeCountsAndPopulationForEveryBydelEveryYear() {
        String[] codes = Arrays.stream(OsloBydel.values()).map(OsloBydel::getCode).toArray(String[]::new);

        for (int year : mock.years()) {
            assertThat(mock.crimeCounts(year)).containsOnlyKeys(codes);
            assertThat(mock.population(year)).containsOnlyKeys(codes);
            assertThat(mock.population(year).values()).allMatch(people -> people > 0);
        }
    }
}
