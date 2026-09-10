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
