# Property

Tar imot en FINN-lenke, leser boligannonsen og sender et `PropertySubmittedEvent` til Kafka (`property-submitted`). Property har ingen database.

## Endepunkt

`POST /api/properties/scrape` med body `{ "url": "https://www.finn.no/realestate/homes/..." }`

Svar: `{ "analysisId": "...", "coordinatesSource": "SCRAPED", "message": null }`

- `400`: ugyldig FINN-lenke
- `502`: annonsen kunne ikke leses og mock er slått av
- `503`: Kafka svarer ikke

`GET /api/properties` er midlertidig og brukes bare av school til den går over til Kafka.

## Kafka-event

Inneholder `analysisId`, `url`, `postalCode`, `latitude`, `longitude`, `coordinatesSource`, `municipalityNumber` og `district`. `district` (bydel) finnes bare for Oslo.

Hver tjeneste har sin egen kopi av eventen. Endres et feltnavn, må det endres hos alle mottakerne samtidig.

## Mock

Kan annonsen ikke leses, brukes hele boligen fra `mock-property.json` (Oslo), og `coordinatesSource` blir `MOCK`. Data fra annonsen og mock blandes aldri.

- `property.mock.fallback-enabled=true`: slå mock av eller på
- `property.mock.force=false`: sett til `true` for å alltid bruke mock (demo)

## Tester

`./mvnw test` trenger verken Docker, Kafka eller nettverk.
