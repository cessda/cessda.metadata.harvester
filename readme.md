# cessda.eqb:cessda.eqb.oaiharvester

## About

The cessda.eqb.harvester is a microservice for harvesting metadata made available by third parties using the  Open Archives Initiatives Protocol for Metadata Harvesting. It can be run standalone as a spring boot application or in a docker environment. 

## Configuration
The following properties are related to the harvesting process and extend the standard spring boot properties.
Each of them can be overwritten in the command line such as 


```bash
java -jar cessda.eqb.oaiharvester.jar --harvester.metadataFormat=ddi32 --server.port=9999  
```

| property | effect  |
| ---------|---------|
| a        |       b |

## Run as Spring Boot Application for testing

To execute the microservice with a defined profile run 

```bash
java -jar cessda.eqb.oaiharvester.jar --spring.profiles.active=ukda 
```
 
##  Remote Configuration

Each instances tries to register itself with a spring boot admin server as indicated in the configuration. If selr-registration fails at startup, a warning is shown that can be safely ignored. 

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

    ```bash    
    # Execute all tests locally with default config
    mvn clean test
    
    # Run the app locally with default config and pre-populated database
    mvn clean spring-boot:run
    # Browse to http://localhost:8080 
    # Stop the app with Ctrl+C
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
    #    volumes:
    #     - ./application.properties:/cessda.eqb.oaiharvester/application.properties
    ```

* Browse to http://localhost:8080
