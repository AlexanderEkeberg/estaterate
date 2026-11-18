package no.school.kafka;

import no.school.dto.DataSource;
import no.school.dto.SchoolData;
import no.school.events.ScoreEvent;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScoreEventPublisherTest {

    @Test
    @SuppressWarnings("unchecked")
    void sendsToScoreEventsWithAnalysisIdAsKey() {
        KafkaTemplate<String, ScoreEvent> kafkaTemplate = mock(KafkaTemplate.class);
        ScoreEvent event = ScoreEvent.failed("a1", "SCHOOL", "error");
        when(kafkaTemplate.send("score-events", "a1", event)).thenReturn(CompletableFuture.completedFuture(null));

        new ScoreEventPublisher(kafkaTemplate).publish(event);

        verify(kafkaTemplate).send("score-events", "a1", event);
    }

    @Test
    @SuppressWarnings("unchecked")
    void doesNotThrowWhenKafkaIsDown() {
        KafkaTemplate<String, ScoreEvent> kafkaTemplate = mock(KafkaTemplate.class);
        ScoreEvent event = ScoreEvent.failed("a1", "SCHOOL", "error");
        when(kafkaTemplate.send("score-events", "a1", event)).thenThrow(new KafkaException("Send failed"));

        assertDoesNotThrow(() -> new ScoreEventPublisher(kafkaTemplate).publish(event));
    }

    @Test
    void serializesToJsonAggregatorCanRead() {
        SchoolData data = new SchoolData("Lakkegata skole", 0.36, DataSource.MOCK);
        ScoreEvent event = ScoreEvent.success("a1", "SCHOOL", 90, data);

        JacksonJsonSerializer<ScoreEvent> serializer = new JacksonJsonSerializer<>();
        serializer.configure(Map.of("spring.json.add.type.headers", false), false);
        RecordHeaders headers = new RecordHeaders();
        String json = new String(serializer.serialize("score-events", headers, event), StandardCharsets.UTF_8);

        assertNull(headers.lastHeader("__TypeId__"));
        assertTrue(json.contains("\"analysisId\":\"a1\""));
        assertTrue(json.contains("\"category\":\"SCHOOL\""));
        assertTrue(json.contains("\"status\":\"SUCCESS\""));
        assertTrue(json.contains("\"score\":90"));
        assertTrue(json.contains("\"nearestSchool\":\"Lakkegata skole\""));
        assertTrue(json.contains("\"dataSource\":\"MOCK\""));
    }

}
