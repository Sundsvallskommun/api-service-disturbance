# Disturbance

## Leverantör

Sundsvalls kommun

## Beskrivning
Disturbance är en tjänst som hanterar aviseringar vid driftsavbrott.


## Tekniska detaljer

### Integrationer
Tjänsten integrerar mot:

* [Messaging](https://github.com/Sundsvallskommun/api-service-messaging) 

### Starta tjänsten

|Miljövariabel|Beskrivning|
|---|---|
|**Databasinställningar**||
|`spring.datasource.url`|JDBC-URL för anslutning till databas|
|`spring.datasource.username`|Användarnamn för anslutning till databas|
|`spring.datasource.password`|Lösenord för anslutning till databas|


### Paketera och starta tjänsten
Applikationen kan paketeras genom:

```
./mvnw package
```
Kommandot skapar filen `api-service-disturbance-<version>.jar` i katalogen `target`. Tjänsten kan nu köras genom kommandot `java -jar target/api-service-disturbance-<version>.jar`. Observera att en lokal databas måste finnas startad för att tjänsten ska fungera.

### Bygga och starta med Docker
Exekvera följande kommando för att bygga en Docker-image:

```
docker build -f src/main/docker/Dockerfile -t api.sundsvall.se/ms-disturbance:latest .
```

Exekvera följande kommando för att starta samma Docker-image i en container:

```
docker run -i --rm -p8080:8080 api.sundsvall.se/ms-disturbance

```

#### Kör applikationen lokalt

<div style='border: solid 1px #0085A9; border-radius: 0.5em; padding: 0.5em 1em; background-color: #D6E0E3; margin: 0 0 0.8em 0 '>
  För att köra applikationen lokalt måste du ha Docker Desktop installerat och startat på din dator.
</div>

Exekvera följande kommando för att bygga och starta en container i sandbox mode:  

```
docker-compose -f src/main/docker/docker-compose-sandbox.yaml build && docker-compose -f src/main/docker/docker-compose-sandbox.yaml up
```


## 
Copyright (c) 2021 Sundsvalls kommun
