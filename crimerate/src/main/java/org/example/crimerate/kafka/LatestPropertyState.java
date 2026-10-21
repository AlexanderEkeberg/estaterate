package org.example.crimerate.kafka;

import org.example.crimerate.events.PropertySubmittedEvent;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public class LatestPropertyState {

    private final AtomicReference<PropertySubmittedEvent> latest = new AtomicReference<>();

    public void update(PropertySubmittedEvent event) {
        latest.set(event);
    }

    public PropertySubmittedEvent getLatest() {
        return latest.get();
    }
}
