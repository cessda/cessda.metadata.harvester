# Euro Question Bank OAI PMH Harvesting Microservice
[![Build Status](https://jenkins.cessda.eu/buildStatus/icon?job=cessda.eqb.metadata.harvester%2Fmaster)](https://jenkins.cessda.eu/job/cessda.eqb.metadata.harvester%2Fmaster)
[![Quality Gate Status](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.eqb:oaiharvester&metric=alert_status)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.eqb:oaiharvester)
[![Coverage](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.eqb:oaiharvester&metric=coverage)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.eqb:oaiharvester)
[![Maintainability Rating](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.eqb:oaiharvester&metric=sqale_rating)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.eqb:oaiharvester)
[![Reliability Rating](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.eqb:oaiharvester&metric=reliability_rating)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.eqb:oaiharvester)
[![Security Rating](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.eqb:oaiharvester&metric=security_rating)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.eqb:oaiharvester)
[![Lines of Code](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.eqb:oaiharvester&metric=ncloc)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.eqb:oaiharvester)

## Summary 

The cessda.eqb.harvester is a microservice for harvesting metadata made available by third parties using the Open Archives Initiatives Protocol for Metadata Harvesting. Please refer to https://www.openarchives.org/OAI/openarchivesprotocol.html for details. It can be run standalone as a spring boot application or in a docker environment.

## Run as Spring Boot Application

To execute the microservice with a defined profile run the service with the `spring.profiles.active` property. The following will run the app with the properties from the [application-cdc.yml](src/main/resources/application-cdc.yml) file 

```bash
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=cdc
```

By default, the harvester will write to `data/` in the current working directory. To change this, use the parameter `--harvester.dir` when starting the harvester.

### Run all configurations (EQB)

```bash
java -jar oaiharvester.jar --spring.profiles.active=csda
java -jar oaiharvester.jar --spring.profiles.active=dans
java -jar oaiharvester.jar --spring.profiles.active=dbk
java -jar oaiharvester.jar --spring.profiles.active=ekke
java -jar oaiharvester.jar --spring.profiles.active=fsd
java -jar oaiharvester.jar --spring.profiles.active=fsd-ddi32
java -jar oaiharvester.jar --spring.profiles.active=nsd-questionConstructs
java -jar oaiharvester.jar --spring.profiles.active=nsd-questions
java -jar oaiharvester.jar --spring.profiles.active=nsd-questionGrids
java -jar oaiharvester.jar --spring.profiles.active=nsd-series
java -jar oaiharvester.jar --spring.profiles.active=nsd-studies
java -jar oaiharvester.jar --spring.profiles.active=nsd
java -jar oaiharvester.jar --spring.profiles.active=snd
java -jar oaiharvester.jar --spring.profiles.active=ukda
```

## Configuration

The following properties are related to the harvesting process and extend the standard spring boot properties.
Each of them can be overwritten in the command line such as 


```bash
java -jar cessda.eqb.oaiharvester.jar --harvester.dir=/example/output/directory
```

### Control the harvesting process

| property                       | effect                    |
| -------------------------------|---------------------------|
| harvester.dir|directory where harvested files will be downloaded to|
| harvester.timeout| seconds to wait until a request is considered erroneous|
| harvester.from.single|controls the `from` parameter when harvesting a single repository|
| harvester.from.intitial|controls the `from` parameter when performing the initial harvesting after application startup|
| harvester.from.incremental|controls the `from` parameter when performing incremental harvesting as defined in the cron expression `harvester.cron.incremental`|
| harvester.from.full|controls the `from` parameter when performing full harvesting |
| harvester.keepOAIEnvelope   | if true, will cause the OAI-PMH response to be written "as is" |
| harvester.removeOAIEnvelope | if true, will remove the OAI-PMH header from the response before writing |
| harvester.repos             | a list of repositories; each with a code, url, metadata prefixes and optionally a list of sets |


#### Define a list of repositories to be harvested

The following configuration will harvest the set `discipline:social-science` of the oai server `https://snd.gu.se/en/oai-pmh` with the metadata prefix `ddi_3_2`.

```yml
harvester:
  repos:
  - url: https://snd.gu.se/en/oai-pmh?verb=ListIdentifiers&set=discipline:social-science
    metadataFormat: ddi_3_2
```

#### Metadata Prefixes

OAI-PMH supports the concepts of different forms of metadata for the same record. This is handled with metadata prefixes.

Multiple metadata formats can be harvested from each repository. The example below will harvest `https://snd.gu.se/en/oai-pmh` using the metadata prefixes `dc`, `ddi` and `ddi_3_2`.

```yaml
harvester:
  repos:
  - url: https://snd.gu.se/en/oai-pmh
    code: SND
    metadataPrefixes: ["dc", "ddi", "ddi_3_2"]
```

Failing to configure any metadata prefixes for a repository will cause the harvest to fail.

## Getting started as developer

* Execute tests and run the application

```bash
# Execute all tests locally with default config
mvn clean test
# Run the app locally with default config
mvn clean spring-boot:run
```

* Create and run service environment with docker-compose

```bash
# Package Java jar file and build docker image with required settings
mvn -DskipTests clean package docker:build -Pdocker-compose
# Create and start the environment in daemon mode (-d)
# as specified in docker-compose.yml
docker-compose -f target/docker/generated/docker-compose.yml up -d
# Show all (-a) containers, service must be healthy to be available
docker ps -a
# Check out with our browser: http://localhost:8080
# Check out with our browser: http://localhost:8080/actuator
# Stop the environment
# All containers and the local network are stopped, but not deleted
docker-compose -f target/docker/generated/docker-compose.yml stop
# Start the existing environment
# All containers and the local network are started
docker-compose -f target/docker/generated/docker-compose.yml start
# Open a shell within the running container as specified in docker-compose.yml
# Exit the container shell again by Ctrl+C
docker exec -it $CONTAINERID /bin/sh
# Checkout filesystem within container with 'ls -la'
# Shutdown the environment
# All containers and the local network are stopped and deleted
docker-compose -f target/docker/generated/docker-compose.yml down
```

## Getting started as user

* Ensure that your Docker host is [correctly configured](https://git.gesis.org/alexander.muehlbauer/dev-env-setup#single-setups-andor-configurations).
* Run a Docker container by 

```bash
docker run -p 8080:8080 docker-private.gesis.intra/gesis/cessda.eqb.oaiharvester:0.0.1-SNAPSHOT
```

* or run a Docker Compose environment by: 

```yml
version: '3.2'
services:
  cessda.eqb.oaiharvester:
    image: docker-private.gesis.intra/gesis/cessda.eqb.oaiharvester:0.0.1-SNAPSHOT
    ports:
     - 8080:8080
    volumes:
     - ./application.properties:/cessda.eqb.oaiharvester/application.properties
```
