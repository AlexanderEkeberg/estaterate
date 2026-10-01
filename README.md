# Temporary README for application setup locally and in Docker

## Local

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



## Docker (USE THIS NOW)

Build and start the services (first time / after changing dockerfiles, code or config):
```bash
docker compose up --build -d
```

Start the services (run if nothing has been changed):
```bash
docker compose up -d
```

Gateway port: [http://localhost:8080](http://localhost:8080) 
<br>
For example do a GET request to [http://localhost:8080/api/properties](http://localhost:8080/api/properties)
