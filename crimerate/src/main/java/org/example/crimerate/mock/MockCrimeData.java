package org.example.crimerate.mock;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
public class MockCrimeData {

    private static final String FILE = "mock-crimerate.json";

    private record File(int year, Map<String, Integer> crimeCounts, Map<String, Integer> population) {
    }

    private final File data;

    public MockCrimeData() {
        try (InputStream file = new ClassPathResource(FILE).getInputStream()) {
            this.data = JsonMapper.builder().build().readValue(file, File.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + FILE, e);
        }
    }

    public int year() {
        return data.year();
    }

    public Map<String, Integer> crimeCounts() {
        return data.crimeCounts();
    }

    public Map<String, Integer> population() {
        return data.population();
    }
}
