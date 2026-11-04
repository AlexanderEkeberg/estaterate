package org.example.crimerate.kafka;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.example.crimerate.dto.CrimeData;
import org.example.crimerate.dto.DataSource;
import org.example.crimerate.events.ScoreEvent;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScoreEventPublisherTest {

    @Test
    @SuppressWarnings("unchecked")
    void sendsToScoreEventsWithAnalysisIdAsKey() {
        KafkaTemplate<String, ScoreEvent> kafkaTemplate = mock(KafkaTemplate.class);
        ScoreEvent event = ScoreEvent.failed("a1", "CRIME", "feil");
        when(kafkaTemplate.send("score-events", "a1", event)).thenReturn(CompletableFuture.completedFuture(null));

        new ScoreEventPublisher(kafkaTemplate).publish(event);

        verify(kafkaTemplate).send("score-events", "a1", event);
    }

    @Test
    @SuppressWarnings("unchecked")
    void doesNotThrowWhenKafkaIsDown() {
        KafkaTemplate<String, ScoreEvent> kafkaTemplate = mock(KafkaTemplate.class);
        ScoreEvent event = ScoreEvent.failed("a1", "CRIME", "feil");
        when(kafkaTemplate.send("score-events", "a1", event)).thenThrow(new KafkaException("Send failed"));

        assertThatCode(() -> new ScoreEventPublisher(kafkaTemplate).publish(event)).doesNotThrowAnyException();
    }

    @Test
    void serializesToJsonWithoutTypeHeader() {
        CrimeData data = new CrimeData(DataSource.LIVE, "Sagene", 2024, 3906, 47627, 82.0, 59.0);
        ScoreEvent event = ScoreEvent.success("a1", "CRIME", 59, data);

        JacksonJsonSerializer<ScoreEvent> serializer = new JacksonJsonSerializer<>();
        serializer.configure(Map.of("spring.json.add.type.headers", false), false);
        RecordHeaders headers = new RecordHeaders();
        String json = new String(serializer.serialize("score-events", headers, event), StandardCharsets.UTF_8);

        assertThat(headers.lastHeader("__TypeId__")).isNull();
        assertThat(json).contains("\"analysisId\":\"a1\"", "\"category\":\"CRIME\"",
                "\"status\":\"SUCCESS\"", "\"score\":59", "\"bydel\":\"Sagene\"", "\"source\":\"LIVE\"");
    }
}
