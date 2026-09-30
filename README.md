# Temporary README for application setup from root

### Terminal 1: District
```bash
cd district
mvn clean spring-boot:run
```

### Terminal 2: Property
```bash
cd property
mvn clean spring-boot:run
```

### Terminal 3: Weather
```bash
cd weather
mvn clean spring-boot:run
```

* **District API:** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) (Port 8081)
* **Property API:** [http://localhost:8090/swagger-ui/index.html](http://localhost:8090/swagger-ui/index.html) (Port 8090)
* **Weather API:** [http://localhost:8082/swagger-ui/index.html](http://localhost:8090/swagger-ui/index.html) (Port 8082)



## Docker 
Build the application JARs from root:

```bash
mvn -f property/pom.xml clean package -DskipTests
mvn -f weather/pom.xml clean package -DskipTests
mvn -f school/pom.xml clean package -DskipTests
mvn -f gateway/pom.xml clean package -DskipTests
```

Start the services:
```bash
docker-compose up
```

Gateway port: [http://localhost:8080](http://localhost:8080) 
<br>
For example do a GET request to [http://localhost:8080/api/properties](http://localhost:8080/api/properties)
