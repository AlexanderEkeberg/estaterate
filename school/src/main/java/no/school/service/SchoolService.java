package no.school.service;

import no.school.events.PropertySubmittedEvent;
import no.school.events.ScoreEvent;

public interface SchoolService {
    ScoreEvent calculate(PropertySubmittedEvent event);
}