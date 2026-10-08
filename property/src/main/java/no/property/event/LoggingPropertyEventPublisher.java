package no.property.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingPropertyEventPublisher implements PropertyEventPublisher {

    @Override
    public void publish(PropertySubmittedEvent event) {
        log.info("Would publish PropertySubmittedEvent: {}", event);
    }
}
