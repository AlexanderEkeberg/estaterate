package no.property.event;

// Hvor boligdataene kommer fra. Navnet beholdes fordi weather og aggregator leser feltet coordinatesSource.
// MOCK betyr at hele boligen er eksempeldata (mock-property.json), aldri bare deler av den.
//bytter dette senere til et navn som forklare det bedre.
public enum CoordinatesSource {
    SCRAPED,
    MOCK
}
