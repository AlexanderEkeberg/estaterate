# Weather – drøftingsnotat

hvordan jeg tenker oppsettet for weather nå i starten

## Hva weather skal gjøre

Ta en eiendom (som property har hentet fra FINN), finne værmeldingen for der eiendommen
ligger, og eksponere den som et enkelt endepunkt frontend kan hente.

## Hele kjeden i systemet

```
Frontend        bruker limer inn FINN-URL til en eiendom
   |
property        henter FINN-siden, parser ut eiendomsdata (postnummer, adresse, evt. koordinater)
   |
weather         posisjon -> Yr/MET locationforecast -> mapper til liten WeatherDto
   |
Frontend        viser eiendom + vær
```

## Flyt inne i weather

1. `PropertyClient` henter eiendommen fra property-service (`property.service.url`)
2. Skaff postkode for eiendommen (eller helst koordinater hvis finnes.)
3. `YrClient` kaller `api.met.no/weatherapi/locationforecast/2.0?lat=..&lon=..`
4. Map Yr sin store JSON-timeserie til en liten `WeatherDto` (kun det frontend trenger)
5. `WeatherController` returnerer `WeatherDto`

Merk: Yr tar koordinater, ikke postnummer. Det er ingen "filtrering" av Yr-data –
vi spør bare om ett punkt, og får data for det området.

så hvis finn annonsen ikke inneholder koordinater og bare post kode må jeg enten opprette en .json fil med 
postkoder = koordinater ferdig oppsatt eller lage en funskjon som gjør om postkode til koordinater.


fant ut at finn annonsene inneholder koordinater for lat og lng som jeg skal bruke i weather. 

vi kan også se på noe som gir brukeren en oversikt over solforhold i området osv.

## Historikk (Open-Meteo) 

`YrClient` gir kun værmelding fremover i tid (nå + noen dager), ikke historikk. Formålet med
historikk-delen er kun å vise brukeren hvordan været har vært siste året i området der boligen
ligger – ikke offisiell/presis målestasjon-data. Vurderte MET sitt **Frost API** først (ekte
norske værstasjoner), men forkastet det: krever registrert client-ID, krever et eget
stasjonssøk-steg før man i det hele tatt kan spørre om data, og stasjonsdekningen er ujevn.
Byttet til **Open-Meteo sitt Historical Weather API** (open-meteo.com) i stedet – ingen nøkkel,
spør direkte på koordinater akkurat som Yr, og dekker hele Norge jevnt via reanalyse-data (ERA5).

### Videre arbeid (TODO)

- Parse Yr og Open-Meteo sine rå JSON-svar til typede modeller i stedet for `String`.
- Map til en tynn, frontend-tilpasset `WeatherDto` (kun feltene frontend faktisk trenger –
  temperatur, symbol-kode, nedbør, månedssnitt) i stedet for å sende rå payloads videre.
- Regn ut månedssnitt av `temperature_2m_max`/`temperature_2m_min` fra de daglige verdiene for
  "typisk måned"-oversikten (Open-Meteo gir ikke dette ferdig aggregert – må gjøres selv, men
  er triviell aggregering av maks 365 tall).
- Postnummer -> koordinater-fallback nevnt over gjelder fortsatt, uavhengig av historikk-delen.