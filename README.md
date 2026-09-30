# Product View Tracking Service

Reactive service that tracks product views, stores them in **MongoDB**, keeps counters and a top list in **Redis**, and
provides user view history.

## Swagger UI

After starting the service: **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
**

---

## Overview

* **Track Product Views** – record a user's view of a product.
* **View Count** – current view count for a product.
* **Top Products** – top N most viewed products.
* **User History** – paginated history of a user's views, newest first.

## API

| Method | Endpoint                              | Description                                             |
|--------|---------------------------------------|---------------------------------------------------------|
| POST   | `/views`                              | Record a view (`productId`, `userId`)                   |
| GET    | `/views/{productId}/count`            | View count for a product                                |
| GET    | `/views/top?limit=5`                  | Top products (`limit` is clamped to 1–100)              |
| GET    | `/views/user/{userId}?page=0&size=50` | User history, newest first (`size` is clamped to 1–200) |

---

## Features

* **Reactive stack**: Spring WebFlux and Project Reactor, non-blocking I/O.
* **MongoDB as the source of truth**: view events are saved first, then the cache is updated.
* **Redis cache (cache-aside)**: counters have a TTL. On a cache miss or Redis failure the count is recalculated from
  MongoDB and cached again.
* **Atomic counter update**: a Lua script increments a counter only if it is already cached and refreshes its TTL, so an
  expired key is never recreated with a wrong value.
* **Top products**: Redis sorted set (`product:top`).
* **Indexes**: index on `productId` for counting, compound index `{userId, viewedAt}` for history queries, and a TTL
  index that removes events after 30 days.
* **Graceful degradation**: Redis errors are logged and do not fail requests.

## Known limitations

* Events in MongoDB expire after 30 days, so a count restored from MongoDB covers the last 30 days, while the Redis top
  list accumulates over time.
* History uses offset pagination, which gets slower on very deep pages (cursor pagination would fix this).

---

## Project Structure

* **config/** – Redis & MongoDB configuration.
* **controller/** – REST API controllers.
* **dto/** – Data transfer objects.
* **mapper/** – Mappers to DTOs.
* **model/** – `ProductViewEvent` entity.
* **repository/** – MongoDB and Redis repositories.
* **service/** – `ProductViewService`.
* **ProductViewTrackingServiceApplication.java** – entry point.

---

## Running the Application

1. Make sure **Docker Desktop** is installed and running.
2. Start all services:

```bash
docker-compose up
```

3. Wait until the containers are ready.
4. Open Swagger UI: `http://localhost:8080/swagger-ui/index.html`

Make sure `spring.data.mongodb.auto-index-creation=true` is set, otherwise MongoDB indexes (including the 30-day TTL)
are not created.

---

## Dependencies

* Spring Boot
* Spring Data MongoDB (Reactive)
* Spring Data Redis (Reactive)
* Project Reactor
* Project Lombok