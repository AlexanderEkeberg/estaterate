package org.example.crimerate.mock;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class MockCrimeData {

    private static final String FILE = "mock-crimerate.json";

    private record Year(Map<String, Integer> crimeCounts, Map<String, Integer> population) {
    }

    private record File(Map<Integer, Year> years) {
    }

    private final File data;

    public MockCrimeData() {
        try (InputStream file = new ClassPathResource(FILE).getInputStream()) {
            this.data = JsonMapper.builder().build().readValue(file, File.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + FILE, e);
        }
    }

    public List<Integer> years() {
        return data.years().keySet().stream().sorted().toList();
    }

    public Map<String, Integer> crimeCounts(int year) {
        return data.years().get(year).crimeCounts();
    }

    public Map<String, Integer> population(int year) {
        return data.years().get(year).population();
    }
}
