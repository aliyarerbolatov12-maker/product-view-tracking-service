---

# Product View Tracking Service

This service tracks product views, stores them in **MongoDB**, caches counts in **Redis**, and provides insights like
top viewed products and user history.

## Swagger UI

After starting the service, access the API documentation here:

**[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

---

## Overview

The service supports:

* **Track Product Views**: Record a user's view of a product.
* **View Count**: Retrieve the current view count for a specific product.
* **Top Products**: Retrieve the top N most viewed products.
* **User History**: Retrieve the history of viewed products for a specific user.

---

## Features

* **Reactive Programming**: Uses Spring WebFlux for non-blocking requests.
* **MongoDB**: Stores product view events with timestamps and indices.
* **Redis**: Caches view counts and manages top viewed products.
* **Error Handling**: Redis failures fall back gracefully to MongoDB.

---

## Project Structure

* **config/** – Redis & MongoDB configuration classes.
* **controller/** – REST API controllers.
* **dto/** – Data transfer objects.
* **mapper/** – Mappers for Redis/MongoDB data to DTOs.
* **model/** – `ProductViewEvent` entity.
* **repository/** – MongoDB and Redis repositories.
* **service/** – Service logic (`ProductViewService`).
* **ProductViewTrackingServiceApplication.java** – Main entry point.

---

## Running the Application

1. Make sure **Docker Desktop** is installed and running.
2. Start all required services with Docker Compose:

```bash
docker-compose up
```

3. Wait until the containers are ready.
4. Open Swagger UI to test the API:

```
http://localhost:8080/swagger-ui/index.html
```

That’s it — the service is now running with **MongoDB**, **Redis**, and the **application**.

---

## Dependencies

* **Spring Boot**
* **Spring Data MongoDB**
* **Spring Data Redis**
* **Project Reactor (Flux/Mono)**
* **Project Lombok**

---