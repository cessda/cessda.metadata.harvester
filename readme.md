# cessda.eqb:cessda.eqb.oaiharvester

## About

The cessda.eqb.harvester is a microservice for harvesting metadata made available by third parties using the Open Archives Initiatives Protocol for Metadata Harvesting. Please refer to https://www.openarchives.org/OAI/openarchivesprotocol.html for details. 
It can be run standalone as a spring boot application or in a docker environment. 




## Run as Spring Boot Application

To execute the microservice with a defined profile run the service with the `spring.profiles.active` property. The following will run the app with the properties from the [application-ukda.yml](https://bitbucket.org/cessda/cessda.eqb.metadata.harvester/src/master/src/main/resources/application-ukda.yml) file 

```bash
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=ukda 
```

### Run all configurations
```bash
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=csda --server.port=8801
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=dans --server.port=8802
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=dbk --server.port=8803
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=ekke --server.port=8804
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=fsd --server.port=8805
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=fsd-ddi32 --server.port=8806
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=nsd-questionConstructs --server.port=8807
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=nsd-questions --server.port=8808
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=nsd-questionGrids --server.port=8809
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=nsd-series --server.port=8810
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=nsd-studies --server.port=8811
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=nsd --server.port=8812
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=snd --server.port=8813
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=ukda --server.port=8814
```



## Configuration
The following properties are related to the harvesting process and extend the standard spring boot properties.
Each of them can be overwritten in the command line such as 


```bash
java -jar cessda.eqb.oaiharvester.jar --harvester.metadataFormat=ddi32 --server.port=9999  
```
### Control the harvesting process

| property                       | effect                    |
| -------------------------------|---------------------------|
| harvester.metadataFormat       | metadataPrefix parameter to use in requests to the oai pmh server|
| harvester.dir|directory where harvested files will be downloaded to|
| harvester.recipient| recipient for error messages sent as email. Requires a valid `spring.mail.host`property|
| harvester.timeout| seconds to wait until a request is considered erroneous|
| harvester.from.single|controls the `from` parameter when harvesting a single repository|
| harvester.from.intitial|controls the `from` parameter when performing the initial harvesting after application startup|
| harvester.from.incremental|controls the `from` parameter when performing incremental harvesting as defined in the cron expression `harvester.cron.incremental`|
| harvester.from.full|controls the `from` parameter when performing full harvesting |
| harvester.cron.full|cron expression to define the points in time when to perform a full harvesting|
| harvester.cron.incremental|cron expression to define the points in time when to perform incremental harvesting|
| harvester.repos|a list of repositories, with a url and setName. Prepend a `-`to indicate a new repo in the list|


#### Define a list of repositories to be harvested

```yml
harvester:
 repos:
  - url: https://dbk.gesis.org/dbkoai/
    setName: DBK
```

#### metadataPrefix

Please be aware, that to harvest different metadata standards (i.e. want to use different metadataPrefix parameters), you need to run one instance per metadata prefix, as it is a global parameter. 
Running these lines will lead to all repositories defined in the configuration to be harvested threee times, once with dc, once with ddi....

```bash
java -jar cessda.eqb.oaiharvester.jar --harvester.metadataFormat=oai_dc --server.port=9997  
java -jar cessda.eqb.oaiharvester.jar --harvester.metadataFormat=ddi25 --server.port=9998  
java -jar cessda.eqb.oaiharvester.jar --harvester.metadataFormat=ddi32 --server.port=9999  
```



## Executing methods via JMX

The following methods can be executed via JMX 

* initialHarvesting: Run initial harvesting. Set from date with key harvester.cron.initial. Can be used to harvest an new repository, after the list of repos has been cleared, and the newly added repo url is set. Don't forget to reset the environment and update application.yml for persistent configuration
* singleHarvesting: Run harvesting on one single repo starting from 'harvester.from.single'. Can be used to harvest an new repository, after the list of repos has been cleared, and the newly added repo url is set. The position corresponds to the number given in the list of repos in the configuration view, starting from 0. See environments tab and search for 'harvester.repos'
* fullHarvesting: Run full harvesting. Set from date with key harvester.cron.full
* bundleHarvesting: Run harvesting on several repo starting from 'harvester.from.single'. Separate more than one repo with comma. Can be used to harvest an new repository, after the list of repos has been cleared, and the newly added repo url is set. The position corresponds to the number given in the list of repos in the configuration view, starting from 0. See environments tab and search for 'harvester.repos'
 
##  Remote Configuration

Each instances tries to register itself with a spring boot admin server as indicated in the configuration. If self-registration fails at startup, a warning is shown that can be safely ignored. 

```log
2019-22-10 16:59:27.218 WARN                      (register)        (ApplicationRegistrator.java:115) - Failed to register application as Application(name=harvester, managementUrl=http:
//KOL19707.gesis.intra:8083/actuator, healthUrl=http://harvester:8083, serviceUrl=http://KOL19707.gesis.intra:8083/) at spring-boot-admin ([http://admin:1111/instances]): I/O error on POST request
```



## Endpoints
 * [REST API](http://localhost:8083/)
 * [HTTP GET /hello-world](http://localhost:8083/hello-world)
 * [HTTP GET /actuator](http://localhost:8083/actuator)
 * [HTTP GET /actuator/health](http://localhost:8083/actuator/health)
 * [HTTP GET /actuator/logfile](http://localhost:8083/actuator/logfile)

## Getting started as developer

* Execute tests and run the application

<code>
    # Execute all tests locally with default config
    mvn clean test
    # Run the app locally with default config and pre-populated database
    mvn clean spring-boot:run
    # Browse to http://localhost:8080 
    # Stop the app with Ctrl+C
</code>

* Create and run service environment with docker-compose

<code>
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
</code>

## Getting started as user

* Ensure that your Docker host is [correctly configured](https://git.gesis.org/alexander.muehlbauer/dev-env-setup#single-setups-andor-configurations).
* Run a Docker container by 

    ```yml
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

* Browse to http://localhost:8080
