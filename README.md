### EstateRate

EstateRate is a property evaluation tool designed to help homebuyers assess and compare the attractiveness of different living locations in Oslo.

### Project Overview

By submitting a property listing URL from Finn.no, EstateRate extracts property and location data and evaluates the surrounding area based on factors such as crime rates, nearby schools, and historical weather conditions. 
Each evaluation factor is handled by a dedicated microservice that gathers and processes relevant data. Evaluations are performed asynchronously using Kafka, allowing individual services to process property data independently. 
Once processed, the frontend presents both the individual factor scores and a dynamically calculated overall score. 
Users can customize the evaluation by excluding factors that are not relevant to them, causing the overall score to recalculate immediately based on their preferences.
EstateRate also allows users to compare evaluated properties side by side, and registered users can save properties as favorites to revisit their evaluations later. 
From a technical standpoint, the application demonstrates both synchronous REST communication and asynchronous event-driven messaging via Kafka, alongside service discovery with Consul.

### System Architecture

![Architecture](./docs/architecture.png)

### User Stories

* **Evaluate Locations:** As a property buyer, I want to submit a Finn.no property listing so that I can evaluate the attractiveness of the property's location before making a purchase decision.
* **Customize Preferences:** As a property buyer, I want to customize which factors are included in the evaluation so that the overall score reflects my preferences.
* **Compare Properties:** As a logged-in property buyer, I want to compare property listings so that I can determine which property best matches my preferences.
* **Save Favorites:** As a logged-in property buyer, I want to save properties as favorites to later access their evaluations.
* **Create Account:** As a new user, I want to create an account so that I can save favorites and compare properties.

Compare and favorites require login. Use the demo user below or create an account with "Sign up" or "Get started" in the frontend.

### Getting Started

#### Prerequisites

* **Docker Desktop** (Recommended: Allocate 10–12 GB of RAM in Docker Desktop settings before running scaled services)
* **Java / Maven**

#### Build & Run Instructions

1. **Configure Maven Projects:**
   Right-click the pom.xml files across the different microservices and select **"Add as Maven Project"**.
2. **Start Docker:**
   Ensure Docker Desktop is open and running.
3. **Build and Start Services:**
   From the project root directory, run:

```bash
   docker compose up -d --build
```

4. **Run with Service Scaling (Optional):**
   To spin up multiple instances of the core microservices, run:

```bash
   docker compose up -d --scale property=2 --scale weather=2 --scale crimerate=2 --scale aggregator=2 --scale school=2
```

#### Stopping services

To stop all running containers and remove the persisted data, run:

```bash
   docker compose down -v
```

and delete the folder named "data" that is saved at the root level of the project

### Accessing & Using the Application

#### Service Endpoints

* **Frontend Application:** http://localhost:5173/
* **Consul Service Discovery UI:** http://localhost:8500/ui/dc1/services

#### Demo Login Credentials

* **Email:** tester@estaterate.local
* **Password:** tester

#### Admin Login Credentials

For the Keycloak admin console at http://localhost:8180:

* **Username:** admin
* **Password:** admin

#### Supported Listings

Paste any of the following Finn.no URLs into the application to generate an evaluation:

| Link                                                            | Address                         |
|-----------------------------------------------------------------|---------------------------------|
| https://www.finn.no/realestate/homes/ad.html?finnkode=478564477 | Niels Juels gate 7, 0272 Oslo   |
| https://www.finn.no/realestate/homes/ad.html?finnkode=476403696 | Sofienberggata 23B, 0558 Oslo   |
| https://www.finn.no/realestate/homes/ad.html?finnkode=480187265 | Sars' gate 60, 0564 Oslo        |
| https://www.finn.no/realestate/homes/ad.html?finnkode=480195246 | Trondheimsveien 51 B, 0560 Oslo |


#### Logging & Monitoring (ELK)

All microservices send structured JSON logs to Logstash, which indexes and stores them in Elasticsearch for querying in Kibana.

#### First-Time Kibana Setup

1. Open Kibana at http://localhost:5601.
2. Navigate to Stack Management → Data Views and create a new data view:
* **Index pattern:** `estaterate-logs-*`
* **Timestamp field:** `@timestamp`
3. Open Discover from the main menu to inspect and search logs.

#### Useful Kibana Search Queries

* `analysis_id : "<id>"` — View every event tied to a specific property evaluation across all microservices.
* `service.name : "crimerate"` — Filter logs from a single service (e.g., crimerate, weather, school, property, aggregator).
* `level : "ERROR"` — Isolate errors and failed operations across the system.
* `event_type : "mock_fallback_used"` — Identify when fallback mock data was used instead of a live external API.



#### Decisions, Assumptions & Simplifications

* FINN blocks automated requests, so the supported listings above are read from mock data. If another listing cannot be read, a default property in Oslo is used instead.

#### Team Contributions & Responsibilities

* **Candidate number 19** – Responsible for the frontend, property, weather, favourites, gateway and Keycloak.
  * **Frontend:** most of the user interface, including analysis, compare, favourites, login and sign-up.
  * **Property:** reading FINN listings, mock data, and publishing the property event to Kafka.
  * **Weather:** forecast and historical weather, published as a score event to Kafka.
  * **Favourites:** saving favourites per user in PostgreSQL.
  * **Gateway:** routing and protected routes with JWT validation.
  * **Keycloak:** authentication setup with realm, demo user, login and sign-up.

* **Candidate number 6** – Responsible for crimerate, logging (ELK) and the crime section in the frontend.
    * **Crimerate:** crime statistics per Oslo district, published as a score event to Kafka.
    * **Integration:** Dockerfile, Consul registration and Kafka consumer for crimerate.
    * **Frontend:** crime section with a chart of the district compared with the Oslo average.
    * **Logging (ELK):** Elasticsearch, Logstash and Kibana, and structured logging in all services.

* **Candidate number 38** - Responsible for Docker setup, aggregator, partly gateway, partly frontend, Kafka integration, Consul, and general functionality.
  * **Docker:** initial containerization and general management of compose.
  * **Aggregator:** gathering data from services and saving in PostgreSQL to serve the frontend.
  * **Gateway:** configuring load-balanced URIs for dynamic routing.
  * **Frontend:** functionality for customizing preferences (disabling factors included in score summary).
  * **Kafka:** adding Kafka integration and initial functionality for PropertySubmitted events.
  * **Consul:** service discovery, health checks, centralized configuration. 
  * **General:** configuring services for scaling (multiple instances), as well as providing a general guideline for the project architecture/flow. 

* **Candidate number 31** - Responsible for the school service
    * **school service:** calculate distance from the nearest school (from api, with mock fallback) publishing score event to Kafka. 
    * **frontend:** built the school card and data mapping for school data. 
    * **General** contributed in discussion about design choices and architecture.
 
#### Checklist of requirements
Required (but not sufficient) for E <br>
[x] 1. Use multiple services, that fulfill different functionality and communicate with each
other <br>
[x] 2. At least two of the services communicate using synchronous communication (for
example, direct REST calls between two services). <br>
[x] 3. At least two of the services communicate using asynchronous communication (for
example, using Message Queue). This will be done in accordance with event-driven
architecture, as discussed in class. <br>
 
Required (but not sufficient) for D <br>
[x] 4. Each service has a clear structure and functionality. <br>
[x] 5. The architecture of the project is consistent with the documentation. (All services are
described in the architecture, they can be found in the project, their functionality, structure,
interactions are documented. <br>
[x] 6. The project can be deployed as a group of Docker containers. <br>

Required (but not sufficient) for C <br>
[x] 7. The project uses a unique access point, that handled calls and routes them to appropriate
services – Gateway <br>
[x] 8. The project uses a unique access point that, in addition to routing calls, also does load
balancing <br>
 
Required (but not sufficient) for B <br>
[x] 9. The project has a means of centrally controlling the health of running services – health
check <br>
[x] 10. The project, and all services and necessary components, can be started via docker
compose (using the command ‘docker compose up’). The project is ready for integration into
a CI/CD pipeline. <br>

Required (but not sufficient) for A <br>
[x] 11. The project has a means of centrally controlling configurations for the services – for
example, using Consul <br>
[x] 12. The project can be deployed with multiple instances of relevant services of the
services. <br>
  
