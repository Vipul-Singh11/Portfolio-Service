# 📊 Portfolio Service

Portfolio Service is a core microservice of the **Stock Trading Simulation Platform**.  
It maintains user stock holdings by consuming trade execution events and updating portfolios in a consistent, event-driven manner.

---

## 🚀 Features

- 📈 Maintain user stock holdings
- 🔄 Event-driven updates via Redis Pub/Sub
- ⚡ Real-time portfolio updates on trade execution
- 🧮 Handles BUY and SELL operations
- 🛡 Idempotent processing (prevents duplicate trade updates)
- 🔁 Transactional consistency using @Transactional
- 🧠 Optimistic locking using @Version
- 🧹 Auto-remove holdings when quantity becomes zero
- 📡 REST APIs for portfolio retrieval and testing
- 🧱 Clean layered architecture
- 📜 Structured logging for traceability

---

## 🛠 Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- MySQL
- Redis (Pub/Sub)
- Lombok

---

## 📂 Project Structure

    com.stock.portfolio_service
    │
    ├── controller         # REST APIs
    ├── service            # Business logic
    │   └── impl
    ├── repository         # JPA repositories
    ├── entity             # Portfolio, ProcessedTrade
    ├── dto                # TradeEventDto, PortfolioResponseDto, PortfolioSummaryDto
    ├── subscriber         # Redis event listener
    ├── client             # External service clients (e.g., Stock Price)
    ├── config             # Redis & Rest configs
    ├── exception          # Custom exceptions & handler
    └── PortfolioServiceApplication.java

---

## ⚙️ Setup Instructions

### 1️⃣ Clone the Repository

    git clone <your-repo-url>
    cd portfolio-service

---

### 2️⃣ Start MySQL (Docker)

    docker run -d \
      --name portfolio-mysql \
      -e MYSQL_ROOT_PASSWORD=root \
      -e MYSQL_DATABASE=portfolio_db \
      -p 3307:3306 \
      mysql:8

---

### 3️⃣ Start Redis (Docker)

    docker run -d \
      --name stock-redis \
      -p 6379:6379 \
      redis

---

### 4️⃣ Configure application.yml

    server:
      port: 8085

    spring:
      application:
        name: portfolio-service

      datasource:
        url: jdbc:mysql://localhost:3307/portfolio_db
        username: root
        password: root

      jpa:
        hibernate:
          ddl-auto: update
        show-sql: true

      redis:
        host: localhost
        port: 6379

---

### 5️⃣ Run the Application

    mvn spring-boot:run

---

## 🔌 API Endpoints

### 📊 Get User Portfolio

    GET /api/portfolio/{userId}

---

### 📈 Get Portfolio Summary

    GET /api/portfolio/summary/{userId}

---

### 🧪 Test Trade (Temporary)

    POST /api/portfolio/trade

#### Request Body:

    {
      "tradeId": 1,
      "buyerUserId": 101,
      "sellerUserId": 102,
      "stockSymbol": "AAPL",
      "quantity": 5,
      "executedPrice": 150,
      "executionTime": "2026-03-27T10:00:00"
    }

---

## 📡 Event-Driven Flow (Redis)

### 🔹 Trade Event Channel

    trade-events

---

### 🧪 Publish Trade via Redis

    docker exec -it stock-redis redis-cli

    PUBLISH trade-events "{\"tradeId\":2,\"buyerUserId\":201,\"sellerUserId\":102,\"stockSymbol\":\"AAPL\",\"quantity\":2,\"executedPrice\":150,\"executionTime\":\"2026-03-27T10:00:00\"}"

---

### ✅ Expected Behavior

- Trade event is received
- Buyer portfolio is increased
- Seller portfolio is decreased
- Portfolio updated in DB
- Duplicate trades are ignored (idempotency)

---

## 🧠 Core Logic

### BUY Operation:
- Increase buyer's stock quantity

### SELL Operation:
- Decrease seller's stock quantity
- Remove record if quantity becomes zero

---

### Idempotency Check:

- Each trade is stored in `processed_trades`
- Duplicate trades are skipped

---

### Transaction Flow:

    Validate trade
    → Check if already processed
    → Update buyer portfolio
    → Update seller portfolio
    → Save processed trade

---

## 🗄 Database Schema

### Portfolio Table

| Field         | Type    | Description              |
|--------------|--------|--------------------------|
| id           | Long   | Primary Key              |
| user_id      | Long   | User ID                  |
| stock_symbol | String | Stock symbol             |
| quantity     | Integer| Number of shares         |
| version      | Long   | Optimistic locking       |

---

### Processed Trades Table

| Field    | Type  | Description              |
|---------|------|--------------------------|
| tradeId | Long | Processed trade ID       |

---

## 🧪 Testing

### 1️⃣ REST API

    POST http://localhost:8085/api/portfolio/trade

---

### 2️⃣ Redis Event

    PUBLISH trade-events "{...}"

---

### 3️⃣ Verify Database

    SELECT * FROM portfolios;

---

## 🧠 Key Concepts Implemented

- Event-driven architecture (Redis Pub/Sub)
- Idempotent consumer pattern
- Transaction management using @Transactional
- Optimistic locking (@Version)
- Clean layered architecture
- Separation of concerns
- DTO pattern
- Centralized exception handling

---

## 🐳 Docker Notes

- MySQL runs on port 3307
- Redis runs on port 6379
- Ensure both are running before starting service

---

## 📌 Future Improvements

- Store average buy price (for real P&L)
- Trade history tracking
- Integration with Stock Price Service (real Redis prices)
- Kafka for scalable messaging
- WebSocket for live portfolio updates
- Distributed locking for concurrency control

---

## 👨‍💻 Author

Vipul Singh
