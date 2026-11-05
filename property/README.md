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

## Demo-annonser (for testing)

FINN blokkerer automatisk henting av annonser. Disse annonsene er lest av manuelt og ligger i `mock-property.json`, så de gir riktige data uten å spørre FINN:

| Lenke | Adresse |
|---|---|
| https://www.finn.no/realestate/homes/ad.html?finnkode=478564477 | Niels Juels gate 7, 0272 Oslo |
| https://www.finn.no/realestate/homes/ad.html?finnkode=476403696 | Sofienberggata 23B, 0558 Oslo |
| https://www.finn.no/realestate/homes/ad.html?finnkode=480187265 | Sars' gate 60, 0564 Oslo |
| https://www.finn.no/realestate/homes/ad.html?finnkode=480195246 | Trondheimsveien 51 B, 0560 Oslo |

## Mock

Andre annonser som ikke kan leses får standardboligen fra `mock-property.json` (Oslo). Både demo-annonser og standardboligen gir `coordinatesSource: MOCK`. Data fra annonsen og mock blandes aldri.

- `property.mock.fallback-enabled=true`: slå mock av eller på
- `property.mock.force=false`: sett til `true` for å alltid bruke mock (demo)

## Tester

`./mvnw test` trenger verken Docker, Kafka eller nettverk.
