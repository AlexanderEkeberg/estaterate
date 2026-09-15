# Property

Property klarer nå å skrape en Finn.no-boligannonse og hente ut postnummer og koordinater

**Endepunkter:**
- `POST /api/properties/scrape` — body: `{ "url": "https://www.finn.no/..." }`. Skraper siden og returnerer et `PropertyLocationDTO`.
- `GET /api/properties` — henter det sist skrapede resultatet. 

**Hvordan det funker:** Finn embedder postnummer/lat/lng som ren tekst inni en `<script>`-tag (`streamController.enqueue(...)`), så vi henter rå HTML (`FinnClient`) og regex-parser ut verdiene (`FinnHtmlParser`) i stedet for å bruke en HTML-parser som Jsoup.
