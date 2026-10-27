package no.weather.kafka;

import no.weather.dto.WeatherData;
import no.weather.events.ScoreEvent;
import no.weather.mock.MockWeatherData;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ScoreEventPublisherTest {

    @Test
    @SuppressWarnings("unchecked")
    void sendsToScoreEventsWithAnalysisIdAsKey() {
        KafkaTemplate<String, ScoreEvent> kafkaTemplate = mock(KafkaTemplate.class);
        ScoreEvent event = ScoreEvent.failed("a1", "WEATHER", "feil");

        new ScoreEventPublisher(kafkaTemplate).publish(event);

        verify(kafkaTemplate).send("score-events", "a1", event);
    }

    // sjekker selve JSON-en scoring får: ingen typeheader, ISO-datoer og score = null
    @Test
    void serializesToJsonScoringCanRead() {
        MockWeatherData mock = new MockWeatherData();
        WeatherData data = new WeatherData(null, null, mock.monthlyAverages(), mock.monthly(), mock.forecast(), null);
        ScoreEvent event = ScoreEvent.success("a1", "WEATHER", data);

        JacksonJsonSerializer<ScoreEvent> serializer = new JacksonJsonSerializer<>();
        serializer.configure(Map.of("spring.json.add.type.headers", false), false);
        RecordHeaders headers = new RecordHeaders();
        String json = new String(serializer.serialize("score-events", headers, event), StandardCharsets.UTF_8);

        assertNull(headers.lastHeader("__TypeId__"));
        assertTrue(json.contains("\"analysisId\":\"a1\""));
        assertTrue(json.contains("\"status\":\"SUCCESS\""));
        assertTrue(json.contains("\"score\":null"));
        assertTrue(json.contains("\"date\":\"" + mock.forecast().get(0).date() + "\""));
        assertEquals(-1, json.indexOf("\"date\":["));
    }
}
