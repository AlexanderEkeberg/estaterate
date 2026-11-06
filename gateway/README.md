Single entry point for the frontend. Routes are resolved through Consul (`lb://<service>`).

POST `/api/properties/scrape`  (property)
GET `/api/analysis/{analysisId}` (aggregator)

All other paths return 404. Weather, crimerate and school communicate over Kafka and have no routes.
