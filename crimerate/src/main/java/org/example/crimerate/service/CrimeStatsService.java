package org.example.crimerate.service;

import org.example.crimerate.events.PropertySubmittedEvent;
import org.example.crimerate.events.ScoreEvent;

public interface CrimeStatsService {

    ScoreEvent calculate(PropertySubmittedEvent event);
}
