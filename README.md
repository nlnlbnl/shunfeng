# Parcel Station MVP

Lightweight parcel station management prototype for the last-mile station workflow. The MVP covers parcel inbound registration, waiting-pickup lookup by recipient phone, pickup confirmation, paginated parcel management, and overdue highlighting for parcels waiting more than 48 hours.

## Tech Stack

- Backend: Java 17+, Spring Boot WebMVC, Spring Validation, Spring JDBC, Maven
- Database: MySQL 8
- Frontend: Vue 3, Vite, Element Plus, Axios, Vue Router
- Tests: JUnit 5, Spring WebMVC Test, H2 for backend test support, frontend production build

The current Maven project uses Spring Boot `4.1.0`, Maven `java.version=17`, and `spring-boot-starter-jdbc`. This avoids relying on MyBatis-Plus Spring Boot 3 starters under Spring Boot 4. The implementation runs on newer JDKs as well; the local verification environment used JDK 18. If MyBatis-Plus is introduced later, verify its Spring Boot 4 compatibility first or document a downgrade to Spring Boot 3.4.x.

## Project Structure

```text
demo/
├── src/                         # Spring Boot backend
├── docs/                        # Local-only docs, ignored by Git
├── frontend/                    # Vue frontend
├── pom.xml
└── README.md
```

## Quick Local Demo

The fastest way to run the full demo is the `dev` profile. It uses an in-memory H2 database, creates the `parcel` table automatically, and inserts sample parcels:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

This starts the backend on `8081`, which matches the frontend Vite proxy in `frontend/vite.config.js`.

## MySQL Database Initialization

Create the MySQL database and table manually:

```sql
CREATE DATABASE IF NOT EXISTS parcel_station
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE parcel_station;

CREATE TABLE IF NOT EXISTS parcel (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tracking_no VARCHAR(64) NOT NULL,
  recipient_phone VARCHAR(20) NOT NULL,
  express_company VARCHAR(64) NOT NULL,
  shelf_location VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'WAITING_PICKUP',
  inbound_time DATETIME NOT NULL,
  outbound_time DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_tracking_no (tracking_no),
  KEY idx_phone_status (recipient_phone, status),
  KEY idx_status_inbound_time (status, inbound_time)
);
```

The backend reads database credentials from environment variables, with local defaults:

```bash
export DB_USERNAME=root
export DB_PASSWORD=
```

The default JDBC URL is:

```text
jdbc:mysql://localhost:3306/parcel_station?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
```

## Backend Startup With MySQL

```bash
./mvnw spring-boot:run
```

If port `8080` is occupied, start on `8081`:

```bash
DB_USERNAME=root DB_PASSWORD= ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

If the parcel list returns `数据库连接失败，请检查数据库服务、用户名和密码`, start the backend with the real MySQL credentials, for example:

```bash
DB_USERNAME=root DB_PASSWORD=<your_mysql_password> ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

Default backend URL:

```text
http://localhost:8080
```

## Frontend Startup

Install dependencies and start the Vite dev server:

```bash
cd frontend
npm install
npm run dev
```

Default frontend URL:

```text
http://localhost:5173
```

## Test Commands

Run backend tests:

```bash
./mvnw test
```

Run frontend production build:

```bash
cd frontend
npm run build
```

Manual verification should cover parcel creation, waiting-pickup lookup, pickup confirmation, paginated list filters, and overdue highlighting.

## API Summary

All API responses use:

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

Endpoints:

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/parcels` | Create an inbound parcel |
| `GET` | `/api/parcels/waiting?phone=13800138000` | List waiting-pickup parcels by recipient phone |
| `PUT` | `/api/parcels/{id}/pickup` | Confirm parcel pickup |
| `GET` | `/api/parcels?page=1&pageSize=10` | Paginated parcel list with optional filters |

Optional list filters:

- `phone`: recipient phone
- `trackingNo`: tracking number query
- `status`: `WAITING_PICKUP` or `PICKED_UP`

The frontend Vite server proxies `/api` requests to `http://localhost:8081` during local development.

## Overdue Rule

`overdue` is not stored in the database. It is calculated dynamically when parcel data is returned:

```text
overdue = status == WAITING_PICKUP && inboundTime + 48 hours < now
```

Picked-up parcels are never marked overdue, even if their inbound time is more than 48 hours ago.

## Status Values

| Status | Meaning |
| --- | --- |
| `WAITING_PICKUP` | Waiting for recipient pickup |
| `PICKED_UP` | Picked up |

The MVP supports only this forward transition:

```text
WAITING_PICKUP -> PICKED_UP
```
