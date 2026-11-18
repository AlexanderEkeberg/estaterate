# Temporary README for application setup locally and in Docker

## Local (Deprecated)

### Terminal 1: Property
```bash
cd property
mvn clean spring-boot:run
```

### Terminal 2: Weather
```bash
cd weather
mvn clean spring-boot:run
```

### Terminal 3: School
```bash
cd school
mvn clean spring-boot:run
```

* **Property API:** [http://localhost:8090/swagger-ui/index.html](http://localhost:8090/swagger-ui/index.html) (Port 8090)
* **Weather API:** [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html) (Port 8082)
* **School API:** [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html) (Port 8083)
* **Crimerate API:** [http://localhost:8084/swagger-ui/index.html](http://localhost:8084/swagger-ui/index.html) (Port 8084)

## Consul
http://localhost:8500/ui/dc1/services


## Docker (USE THIS NOW)

Build and start the services (first time / after changing dockerfiles, code or config):
```bash
docker compose up -d --build 
```

Start the services (run if nothing has been changed):
```bash
docker compose up -d 
```

To get a clean sheet and clear the aggregator database:
```bash
docker compose down -v
```
and delete the "data" folder at root level

Start the services with scaling:
```bash
docker compose up -d --scale property=2 --scale weather=2 --scale crimerate=2 --scale aggregator=2 --scale school=2
```

Gateway port: [http://localhost:8080](http://localhost:8080) 
<br>
For example do a GET request to [http://localhost:8080/api/properties](http://localhost:8080/api/properties)
Gateway: `localhost:8080`. Routes are listed in `gateway/README.md`.


## Logging (ELK)

All services send structured JSON logs to Logstash, which stores them in Elasticsearch. Kibana is used to search them.

Docker needs about 10 GB of memory to run all services together with ELK. With the default 8 GB, Elasticsearch can be stopped by Docker.

Open Kibana at [http://localhost:5601](http://localhost:5601). The first time, create a data view under Stack Management → Data Views with index pattern `estaterate-logs-*` and timestamp field `@timestamp`. Then go to Discover to search the logs.

Example searches in Kibana:
- `analysis_id : "<id>"` shows every event for one analysis across all services
- `service.name : "crimerate"` shows the logs from one service
- `level : "ERROR"` shows what failed
- `event_type : "mock_fallback_used"` shows when mock data was used instead of a live API


## Postman

Link to import postman requests:
https://richard-1476962.postman.co/workspace/Richard's-Workspace~c2ffdcc7-39f2-41cd-8bc0-30991787d344/collection/44499091-6da4f4cd-cae1-406e-ab61-140674cd3b51?action=share&creator=44499091

Do the POST request first to gather the data from Finn. The GET requests can be used afterwards 


