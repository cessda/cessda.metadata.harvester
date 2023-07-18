# CESSDA OAI-PMH Metadata Harvester

[![Build Status](https://jenkins.cessda.eu/buildStatus/icon?job=cessda.eqb.metadata.harvester%2Fmain)](https://jenkins.cessda.eu/job/cessda.eqb.metadata.harvester%2Fmain)
[![Quality Gate Status](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.eqb:oaiharvester&metric=alert_status)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.eqb:oaiharvester)
[![Coverage](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.eqb:oaiharvester&metric=coverage)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.eqb:oaiharvester)
[![Maintainability Rating](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.eqb:oaiharvester&metric=sqale_rating)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.eqb:oaiharvester)
[![Reliability Rating](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.eqb:oaiharvester&metric=reliability_rating)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.eqb:oaiharvester)
[![Security Rating](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.eqb:oaiharvester&metric=security_rating)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.eqb:oaiharvester)
[![Lines of Code](https://sonarqube.cessda.eu/api/project_badges/measure?project=eu.cessda.eqb:oaiharvester&metric=ncloc)](https://sonarqube.cessda.eu/dashboard?id=eu.cessda.eqb:oaiharvester)

## Summary

The CESSDA metadata harvester is a microservice for harvesting metadata made
available by third parties using the Open Archives Initiatives Protocol for
Metadata Harvesting. Please refer to
<https://www.openarchives.org/OAI/openarchivesprotocol.> for details. It can
be run standalone as a spring boot application or in a docker environment.

## Compile the application

To compile the application to a JAR archive, run the following command:

```bash
./mvnw verify
```

## Run the application

To execute the microservice with a defined profile run the service with the
`spring.profiles.active` property. The following will run the app with the
properties from the
[application-cdc.yml](src/main/resources/application-cdc.yml) file

```bash
java -jar target/oaiharvester.jar --spring.profiles.active=cdc
```

By default, the harvester will write to `data/` in the current working
directory. To change this, use the parameter `--harvester.dir` when starting
the harvester.

## Configuration

The following properties are related to the harvesting process and
extend the standard spring boot properties.
Each of them can be overwritten in the command line such as

```bash
java -jar oaiharvester.jar --harvester.dir=/example/output/directory
```

### Control the harvesting process

Configuration properties that control the harvesting process are all under the `harvester` key.

| Property                     | Type      | Description                                                                                                                                 |
|------------------------------|-----------|---------------------------------------------------------------------------------------------------------------------------------------------|
| `harvester.dir`              | Path      | Directory where harvested files will be written to.                                                                                         |
| `harvester.timeout`          | Duration  | Seconds to wait until an individual HTTP request is cancelled, defaults to 30 seconds.                                                      |
| `harvester.incremental`      | boolean   | Enables incremental harvesting. By default, this will harvest records from the last week unless overridden by `harvester.from.incremental`. |
| `harvester.from.incremental` | LocalDate | If harvester.incremental is true, only records modified after this date will be harvested. This accepts an ISO date as a parameter.         |
| `harvester.repos`            | Repo      | A list of repositories. [See below for a description on how to configure a repository.](#repository-configuration-specification)            |

See <https://www.openarchives.org/OAI/openarchivesprotocol.html#SelectiveHarvestingandDatestamps>
for a description on how incremental harvesting works at the OAI-PMH protocol level.

#### Define a list of repositories to be harvested

The following configuration will harvest the set
`discipline:social-science` of the oai server
`https://snd.gu.se/en/oai-pmh` with the metadata prefix `ddi_3_2`.

```yml
harvester:
  repos:
    - url: https://snd.gu.se/en/oai-pmh
      code: SND
      metadataPrefixes:
        - metadataPrefix: ddi_3_2
          setSpec: subject:social-sciences
```

#### Metadata Prefixes

OAI-PMH supports the concepts of different forms of metadata for the
same record. This is handled with metadata prefixes.

Multiple metadata formats can be harvested from each repository. The
example below will harvest `https://snd.gu.se/en/oai-pmh` using the
metadata prefixes `dc`, `ddi` and `ddi_3_2`.

```yaml
harvester:
  repos:
    - url: https://snd.gu.se/en/oai-pmh
      code: SND
      metadataPrefixes:
        - metadataPrefix: dc
        - metadataPrefix: ddi
        - metadataPrefix: ddi_3_2
```

Failing to configure any metadata prefixes for a repository will cause
the harvest to fail.

#### Sets

OAI-PMH can group multiple records together into a collection of themed records known as a set.
Sets can either be automatically detected or specified explicitly.
Note that a record may be harvested multiple times if it is contained in multiple sets.

To automatically discover and harvest all sets from a repository, set
`discoverSets` to `true` in the repository definition. The following
definition will harvest from all sets using the metadata prefixes
`dc`, `ddi` and `ddi_3_2`.

```yaml
harvester:
  repos:
    - url: https://snd.gu.se/en/oai-pmh
      code: SND
      discoverSets: true
      metadataPrefixes:
        - metadataPrefix: dc
        - metadataPrefix: ddi
        - metadataPrefix: ddi_3_2
```

Sets can also be configured explicitly by defining the name of the set
to be harvested. This involves adding a `setSpec` parameter for each
configured metadata prefix. Metadata prefixes can be restated multiple
times to harvest multiple sets using the same metadata prefix.

```yaml
harvester:
  repos:
    - url: https://snd.gu.se/en/oai-pmh
      code: SND
      metadataPrefixes:
        - metadataPrefix: ddi
          setSpec: subject:history
        - metadataPrefix: ddi
          setSpec: subject:social-sciences
        # This will harvest all record that have a ddi_3_2 metadata prefix
        - metadataPrefix: ddi_3_2
```

Explicitly defining a set under `metadataPrefixes` will override automatic `discoverSets` detection.

#### Pipeline metadata

In order for records to be correctly consumed by the CESSDA Metadata
Pipeline, extra metadata needs to be stated in the repository
configuration. This includes the validation profile and gate, the DDI
version harvested, the name to be displayed in the CDC user interface
and other information. An example repository definition is shown
below.

```yaml
harvester:
  repos:
    - url: https://snd.gu.se/en/oai-pmh
      code: SND
      validationGate: BASIC
      metadataPrefixes:
        - metadataPrefix: oai_ddi25
          setSpec: subject:social-sciences
          ddiVersion: DDI_2_5
          validationProfile: https://cmv.cessda.eu/profiles/cdc/ddi-2.5/latest/profile.xml
```

#### Repository Configuration Specification

| Field              | Type           | Description                                                                               |
|--------------------|----------------|-------------------------------------------------------------------------------------------|
| `code`             | String         | The short name of the repository.                                                         |
| `name`             | String         | The friendly name of the repository, displayed in the user interface.                     |
| `url`              | URI            | The base URL of the OAI-PMH repository.                                                   |
| `discoverSets`     | boolean        | Determines whether the repository should harvest each set in the repository individually. |
| `defaultLanguage`  | String         | The language to treat metadata if unspecified.                                            |
| `validationGate`   | String         | The CESSDA Metadata Validator validation gate to use when validating.                     |
| `metadataPrefixes` | MetadataFormat | Specific harvesting configuration.                                                        |

##### `MetadataFormat` Specification

| Field               | Type   | Description                                                         |
|---------------------|--------|---------------------------------------------------------------------|
| `metadataPrefix`    | String | The metadata prefix to harvest. This is a mandatory parameter.      |
| `setSpec`           | String | The set to harvest, can be omitted.                                 |
| `ddiVersion`        | String | The version of DDI harvested. This is currently unused.             |
| `validationProfile` | URI    | The URI to the CMV validation profile to validate metadata against. |

See [Repo.java](/src/main/java/eu/cessda/oaiharvester/Repo.java) for the concrete implementation and JavaDoc.

## Getting started as developer

### Execute tests and run the application

```bash
# Execute all tests locally with default config
mvn test
# Run the app locally with default config
mvn spring-boot:run
```

### Create and run service environment with docker-compose

```bash
# Package Java jar file and build docker image with required settings
mvn package docker:build -Pdocker-compose
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

## Getting started with Docker

* Ensure that Java, Maven and are installed.
* Build the project using `mvn package docker:build -Pdocker-compose`
* Run a Docker container by

```bash
docker run -p 8080:8080 cessda/oaiharvester
```
