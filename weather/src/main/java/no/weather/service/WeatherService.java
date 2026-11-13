package no.weather.service;

import no.weather.events.PropertySubmittedEvent;
import no.weather.events.ScoreEvent;

public interface WeatherService {

    // sender alltid tilbake en ScoreEvent, også når noe går galt
    ScoreEvent calculate(PropertySubmittedEvent event);
}
