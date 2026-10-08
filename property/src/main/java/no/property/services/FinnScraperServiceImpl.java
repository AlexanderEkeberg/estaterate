package no.property.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.property.client.FinnClient;
import no.property.dto.PropertyLocationDTO;
import no.property.events.PropertySubmittedEvent;
import no.property.kafka.PropertySubmittedProducer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinnScraperServiceImpl implements FinnScraperService {

    private final FinnClient finnClient;
    private final FinnHtmlParser finnAdHtmlParser;
    private final PropertySubmittedProducer eventProducer;

    //Vurdere sener eom vi skal endre til database isteden for in-memory
    //TODO: fungerer kun med én kjørende instans, må løses før property skaleres
    private PropertyLocationDTO latest;

    // KOMMENTERT UT DENNE FOR Å REPLACE MED KAFKA EVENT
//    @Override
//    public PropertyLocationDTO scrape(String url) {
//        String html = finnClient.fetchFinnHtml(url);
//        PropertyLocationDTO result = finnAdHtmlParser.parse(html, url);
//        this.latest = result;
//        return result;
//    }

    public PropertyLocationDTO scrape(String url) {
        String html = finnClient.fetchFinnHtml(url);
        PropertyLocationDTO result = finnAdHtmlParser.parse(html, url);

        UUID propertyId = UUID.randomUUID();

        PropertySubmittedEvent event = new PropertySubmittedEvent(
                UUID.randomUUID(),
                "PropertySubmitted",
                1,
                propertyId,
                result.getUrl(),
                result.getPostalCode(),
                Double.valueOf(result.getLatitude()),
                Double.valueOf(result.getLongitude()),
                Instant.now()
        );

        eventProducer.send(event);

        this.latest = result;
        return result;
    }

    @Override
    public PropertyLocationDTO getLatest() {
        return latest;
    }
}
