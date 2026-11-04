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
* **Crimerate API:** [http://localhost:8084/swagger-ui/index.html](http://localhost:8084/swagger-ui/index.html) (Port 8084)



## Docker (USE THIS NOW)

Build and start the services (first time / after changing dockerfiles, code or config):
```bash
docker compose up -d --build --scale property=3 --scale weather=3 --scale school=3 --scale crimerate=3
```

Start the services (run if nothing has been changed):
```bash
docker compose up -d --scale property=3 --scale weather=3 --scale school=3 --scale crimerate=3
```

Gateway port: [http://localhost:8080](http://localhost:8080) 
<br>
For example do a GET request to [http://localhost:8080/api/properties](http://localhost:8080/api/properties)


## Postman

Link to import postman requests:
https://richard-1476962.postman.co/workspace/Richard's-Workspace~c2ffdcc7-39f2-41cd-8bc0-30991787d344/collection/44499091-6da4f4cd-cae1-406e-ab61-140674cd3b51?action=share&creator=44499091

Do the POST request first to gather the data from Finn. The GET requests can be used afterwards (The GET for school is currently not working since it has not been connected properly yet)


