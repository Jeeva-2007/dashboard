# Master Server

This is the Master Server component of the Server Monitoring System. It is built using Spring Boot.

## Technologies Used
- Java 21
- Spring Boot (Web, WebSocket, Data JPA)
- H2 Database

## Running the Application

You can use Maven wrapper to run the application:

```bash
./mvnw spring-boot:run
```

On Windows, use:

```cmd
.\mvnw.cmd spring-boot:run
```

## Structure
The server handles connections and provides an endpoint/websocket interface for monitoring agents.
