# E-Commerce Microservices Platform

A modern, fully containerized e-commerce platform built on a **microservices architecture** using **Java 21 with Spring Boot** on the backend and **React with TanStack Start** on the frontend. The system features event-driven communication via **Apache Kafka**, synchronous REST APIs, **JWT-based authentication** with service account support, and **Mercado Pago** payment integration.
This is just a MVP developed using only free plan AI's like deepseek and loveable free plan. That project don't aim to be in production, it is just for system design studies.

> **Status**: Active Development  
> **Java Version**: 21  
> **Spring Boot Version**: 3.5.14  
> **Frontend**: React 19 + TanStack Start + TypeScript  
> **Containerization**: Docker & Docker Compose

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Services](#services)
3. [Communication Protocols](#communication-protocols)
   - [REST API](#rest-api)
   - [Apache Kafka (Event-Driven)](#apache-kafka-event-driven)
4. [Database Schema](#database-schema)
5. [Features](#features)
6. [Technology Stack](#technology-stack)
7. [Getting Started](#getting-started)
9. [Justification of Technical Choices](#justification-of-technical-choices)

---

## Architecture Overview

The platform follows a **microservices architecture** where each business capability is encapsulated in its own independently deployable service. Services communicate **synchronously** via REST for request-response operations (e.g., fetching product data during order creation) and **asynchronously** via Apache Kafka for event-driven workflows (e.g., notifying users when an order is confirmed or a payment expires).

```
┌─────────────┐
│   Frontend  │  TanStack Start (React 19) - Port 3000
└──────┬──────┘
       │ REST
┌──────▼──────────────────────────────────────────────┐
│              Gateway Service (Port 8086)             │
│           Spring Cloud Gateway - Route & Auth        │
└──┬────┬────┬────┬────┬────┬────┬────────────────────┘
   │    │    │    │    │    │    │
   ▼    ▼    ▼    ▼    ▼    ▼    ▼
 Auth  User Catalog Order Payment Notif
  :8080 :8081 :8082  :8083  :8084  :8085
   │    │    │    │    │    │    │
   └────┴────┴────┴────┴────┴────┘
              │ Kafka
         ┌────▼────┐
         │  Kafka  │  Event Bus
         └─────────┘
```

**Key architectural principles:**

- **Database-per-Service**: Each microservice has its own dedicated PostgreSQL database, ensuring loose coupling and data isolation.
- **API Gateway Pattern**: A single entry point (Spring Cloud Gateway) handles routing, cross-cutting concerns, and Swagger/OpenAPI aggregation.
- **Event-Driven Communication**: Apache Kafka enables asynchronous, reliable event propagation between services for workflows like order confirmation and payment processing.
- **Service-to-Service Auth**: Internal communication uses a **Service Account** authentication mechanism with dedicated JWT tokens, separate from user authentication.

---

## Services

### 1. Gateway Service (`Gateway-Service`)
- **Port**: `8086`
- **Role**: API Gateway built with **Spring Cloud Gateway**
- **Responsibilities**:
  - Route external requests to internal microservices
  - Aggregate Swagger/OpenAPI docs for all services under unified paths
  - Expose health, info, and gateway management endpoints
- **Routes**:
  - `/api/v1/auth/**` → Auth Service
  - `/api/v1/users/**` → User Service
  - `/api/v1/products/**` → Catalog Service
  - `/api/v1/orders/**` → Order Service
  - `/api/v1/payments/**` → Payment Service
  - `/{service}-swagger/**` → Individual service API docs

### 2. Auth Service (`Auth-Service`)
- **Port**: `8080`
- **Role**: Authentication and Authorization
- **Responsibilities**:
  - Register new users (clients, sellers, admins)
  - Authenticate users via email/password (JWT)
  - Authenticate service accounts for inter-service communication
  - Delete user accounts
- **Entities**: `users`, `service_accounts`

### 3. User Service (`User-Service`)
- **Port**: `8081`
- **Role**: User profile management
- **Responsibilities**:
  - Store and manage user profile data (name, CPF, phone, email)
  - Provide contact information to other services
  - Consume user-created/user-deleted events from Kafka
- **Entities**: `users`

### 4. Catalog Service (`Catalog-Service`)
- **Port**: `8082`
- **Role**: Product catalog management
- **Responsibilities**:
  - CRUD operations for products
  - Support for product attributes (JSONB), image URLs (JSONB), categories, SKUs
  - Stock management (deduct stock on order confirmation via Kafka consumer)
  - Fetch products by IDs (used by Order Service)
- **Entities**: `products`

### 5. Order Service (`Order-Service`)
- **Port**: `8083`
- **Role**: Order management
- **Responsibilities**:
  - Create orders with products grouped by seller (multi-seller support)
  - Fetch product data synchronously from Catalog Service via REST
  - Listen to `payment-confirmed` events to confirm orders
  - Publish `order-confirmed` / `order-cancelled` events to Kafka
- **Entities**: `orders`, `seller_orders`, `order_items`

### 6. Payment Service (`Payment-Service`)
- **Port**: `8084`
- **Role**: Payment processing
- **Responsibilities**:
  - Create payment transactions linked to orders
  - Integrate with **Mercado Pago** SDK for payment gateway operations
  - Publish `payment-confirmed` / `payment-expired` events to Kafka
- **Entities**: `payments`
- **Payment Methods**: Supports multiple payment methods via Mercado Pago

### 7. Notification Service (`Notification-Service`)
- **Port**: `8085`
- **Role**: User notifications
- **Responsibilities**:
  - Consume Kafka events for order and payment status changes
  - Fetch user contact info from User Service via REST
  - Send email notifications to users
- **Note**: Stateless service, no database

### 8. ecommerce-common (`ecommerce-common`)
- **Role**: Shared library
- **Contents**: Kafka event definitions shared across services:
  - `OrderConfirmedEvent` / `OrderCancelledEvent`
  - `PaymentConfirmedEvent` / `PaymentExpiredEvent`
  - `UserCreatedEvent` / `UserDeletedEvent`

### 9. Frontend
- **Port**: `3000`
- **Stack**: React 19, TanStack Start (SSR), TanStack Router, TanStack Query
- **UI**: Tailwind CSS 4, Radix UI primitives, shadcn/ui components
- **Features**: Form handling (react-hook-form + zod), charts (recharts), responsive design

---

## Communication Protocols

### REST API

Synchronous HTTP communication is used for request-response patterns where immediate feedback is required.

| Endpoint Pattern | Source → Destination | Purpose |
|---|---|---|
| `POST /api/v1/auth/login` | Client → Auth | User authentication |
| `POST /api/v1/auth/register` | Client → Auth | User registration |
| `POST /api/v1/auth/service-account/login` | Service → Auth | Internal service authentication |
| `GET /api/v1/users/contact-info/{id}` | Notification → User | Fetch user email/phone |
| `GET /api/v1/products` | Client → Catalog | List/search products |
| `POST /api/v1/products/batch` | Order → Catalog | Fetch product details by IDs |
| `POST /api/v1/orders` | Client → Order | Create order |
| `GET /api/v1/orders/{id}` | Client / Payment → Order | Get order details |
| `POST /api/v1/payments` | Client → Payment | Create payment |
| `PUT /api/v1/payments/{id}/confirm` | Mercado Pago → Payment | Payment webhook callback |

All REST communication flows through the **Gateway Service**, which handles routing and acts as a reverse proxy. Inter-service REST calls (e.g., Order → Catalog, Notification → User) use **RestTemplate** with **Bearer Token Interceptors** that attach service account JWT tokens for authentication.

### Apache Kafka (Event-Driven)

Asynchronous event-driven communication is used for workflows spanning multiple services, providing **loose coupling**, **fault tolerance**, and **reliability**.

| Event | Producer | Consumers | Purpose |
|---|---|---|---|
| `user-created` | Auth Service | User Service | Sync user profile data after registration |
| `user-deleted` | Auth Service | User Service, Notification Service | Cleanup user data across services |
| `payment-confirmed` | Payment Service | Order Service, Notification Service | Confirm order & notify user |
| `payment-expired` | Payment Service | Order Service, Notification Service | Cancel order & notify user |
| `order-confirmed` | Order Service | Catalog Service, Notification Service | Deduct stock & notify user |
| `order-cancelled` | Order Service | Catalog Service, Notification Service | Restore stock & notify user |

**Why Kafka?**
- **Durability**: Persistent logs ensure no events are lost, even if consumers are temporarily offline.
- **Scalability**: Multiple consumer groups can process the same event stream independently.
- **Decoupling**: Producers and consumers are completely independent — the Order Service doesn't need to know about the Notification Service.
- **Ordering**: Messages within a partition are ordered, which is important for consistency in state transitions.

---

## Database Schema

Each microservice has its own dedicated PostgreSQL database.

### Auth Service (`AuthService`)

**Table: `users`**
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, Generated |
| `email` | VARCHAR(100) | NOT NULL, UNIQUE |
| `password` | VARCHAR(200) | NOT NULL |
| `role` | VARCHAR(25) | NOT NULL (ROLE_CLIENT, ROLE_SELLER, ROLE_ADMIN) |

**Table: `service_accounts`**
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, Generated |
| `client_id` | VARCHAR(100) | NOT NULL, UNIQUE |
| `client_secret_hash` | VARCHAR(200) | NOT NULL |
| `role` | VARCHAR(25) | NOT NULL (ROLE_SERVICE, ROLE_ORDER_SERVICE, etc.) |
| `enabled` | BOOLEAN | NOT NULL, DEFAULT TRUE |

### User Service (`UserService`)

**Table: `users`**
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK |
| `name` | VARCHAR(100) | NOT NULL |
| `cpf` | VARCHAR(11) | NOT NULL, UNIQUE |
| `phone_number` | VARCHAR(20) | NOT NULL |
| `email` | VARCHAR | NOT NULL, UNIQUE |

### Catalog Service (`CatalogService`)

**Table: `products`**
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, Generated |
| `seller_id` | UUID | NOT NULL |
| `name` | VARCHAR(200) | NOT NULL |
| `description` | TEXT | |
| `price` | DECIMAL(19,2) | NOT NULL |
| `stock` | INTEGER | NOT NULL |
| `active` | BOOLEAN | NOT NULL |
| `category` | VARCHAR | NOT NULL |
| `sku` | VARCHAR | NOT NULL |
| `attributes` | JSONB | Product-specific attributes |
| `image_urls` | JSONB | Array of image URLs |
| `created_at` / `updated_at` | TIMESTAMP | Auditing |

### Order Service (`OrderService`)

**Table: `orders`**
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, Generated |
| `client_id` | UUID | NOT NULL |
| `status` | VARCHAR(30) | NOT NULL (PENDING_PAYMENT, CONFIRMED, CANCELLED) |
| `total_price` | DECIMAL(19,2) | NOT NULL |
| `created_at` / `updated_at` | TIMESTAMP | Auditing |

**Table: `seller_orders`**
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, Generated |
| `order_id` | UUID | FK → orders(id) |
| `seller_id` | UUID | NOT NULL |
| `status` | VARCHAR(30) | NOT NULL (PENDING_PAYMENT, CONFIRMED, CANCELLED) |
| `sub_total` | DECIMAL(19,2) | NOT NULL |
| `created_at` / `updated_at` | TIMESTAMP | Auditing |

**Table: `order_items`**
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, Generated |
| `seller_order_id` | UUID | FK → seller_orders(id) |
| `product_id` | UUID | NOT NULL |
| `product_name` | VARCHAR(200) | NOT NULL |
| `unit_price` | DECIMAL(19,2) | NOT NULL |
| `quantity` | INTEGER | NOT NULL |
| `created_at` | TIMESTAMP | Auditing |

### Payment Service (`PaymentService`)

**Table: `payments`**
| Column | Type | Constraints |
|---|---|---|
| `id` | UUID | PK, Generated |
| `order_id` | UUID | NOT NULL |
| `client_id` | UUID | NOT NULL |
| `total_price` | DECIMAL(19,2) | NOT NULL |
| `status` | VARCHAR(30) | NOT NULL (PENDING, CONFIRMED, FAILED, REFUNDED) |
| `payment_method` | VARCHAR(20) | NOT NULL |
| `mp_payment_id` | BIGINT | UNIQUE, Mercado Pago ID |
| `mp_preference_id` | VARCHAR(255) | Mercado Pago preference ID |
| `mp_status` | VARCHAR(30) | Mercado Pago status |
| `mp_init_point` / `mp_sandbox_init_point` | VARCHAR(255) | Payment URLs |
| `created_at` / `updated_at` | TIMESTAMP | Auditing |

---

## Features

### 🔐 Authentication & Authorization
- **User Registration & Login**: JWT-based authentication with role support (CLIENT, SELLER, ADMIN)
- **Service Account Authentication**: Machine-to-machine authentication for inter-service communication using dedicated credentials
- **JWT with Refresh Tokens**: Secure token management with configurable expiration

### 👤 User Management
- **Profile Management**: Store and manage user personal data (name, CPF, phone, email)
- **Event-Driven Sync**: User data is synchronized between Auth Service and User Service via Kafka events

### 📦 Product Catalog
- **Full CRUD**: Create, read, update, and delete products
- **Rich Product Data**: JSONB attributes and image URLs for flexible product configuration
- **Stock Management**: Automatic stock deduction on order confirmation via Kafka consumer
- **Multi-seller Support**: Products are linked to sellers, enabling a marketplace model
- **Search & Filter**: SKU, category, and active status filtering

### 🛒 Order Management
- **Multi-Seller Orders**: One order can contain products from multiple sellers, automatically split into seller-specific sub-orders
- **Status Lifecycle**: PENDING_PAYMENT → CONFIRMED / CANCELLED with proper state validation
- **Pagination & Sorting**: Orders are paginated and sorted by creation date
- **Event-Driven Transitions**: Orders automatically transition state based on payment events

### 💳 Payment Processing (Mercado Pago)
- **Mercado Pago Integration**: Full integration with the Mercado Pago SDK for payment gateway operations
- **Multiple Statuses**: PENDING, CONFIRMED, FAILED, REFUNDED
- **Payment Method Tracking**: Stores payment method and Mercado Pago metadata
- **Webhook Ready**: Supports payment confirmation callbacks from Mercado Pago

### 📧 Email Notifications
- **Event-Driven Emails**: Sends email notifications for:
  - Order confirmation
  - Order cancellation
  - Payment confirmation
  - Payment expiration
- **User Contact Integration**: Fetches user email/phone from User Service via REST before sending

### 🚀 Infrastructure
- **Containerized**: All services run in Docker containers with a single `docker-compose up` command
- **Database-per-Service**: Each service has its own PostgreSQL database instance for data isolation
- **Shared Network**: All services communicate over a dedicated Docker bridge network

### 📚 API Documentation
- **OpenAPI/Swagger**: Each service exposes its own Swagger UI
- **Gateway Aggregation**: All Swagger UIs accessible through the Gateway Service at `/{service}-swagger/**`

---

## Technology Stack

### Backend
| Technology | Purpose |
|---|---|
| **Java 21** | Runtime |
| **Spring Boot 3.5.14** | Framework |
| **Spring Cloud Gateway** | API Gateway |
| **Spring Security** | Authentication & Authorization |
| **Spring Data JPA** | Database access |
| **Spring Kafka** | Event streaming |
| **Apache Kafka** | Message broker |
| **PostgreSQL 17** | Relational database |
| **JWT (jjwt)** | Token-based authentication |
| **Mercado Pago SDK** | Payment gateway integration |
| **ModelMapper** | DTO mapping |
| **SpringDoc OpenAPI** | API documentation |
| **Lombok** | Boilerplate reduction |
| **JUnit 5** | Testing |

### Frontend
| Technology | Purpose |
|---|---|
| **React 19** | UI framework |
| **TanStack Start** | SSR framework |
| **TanStack Router** | Client-side routing |
| **TanStack Query** | Server state management |
| **TypeScript** | Type safety |
| **Tailwind CSS 4** | Utility-first styling |
| **Radix UI** | Accessible UI primitives |
| **shadcn/ui** | Component library |
| **react-hook-form + Zod** | Form validation |
| **recharts** | Charting |
| **Vite** | Build tool |

### Infrastructure
| Technology | Purpose |
|---|---|
| **Docker** | Containerization |
| **Docker Compose** | Orchestration |
| **Git** | Version control |

---

## Getting Started

### Prerequisites
- [Docker](https://docs.docker.com/get-docker/)
- [Docker Compose](https://docs.docker.com/compose/install/)
- Java 21 (for local development)
- Maven (for local development)

### Running with Docker Compose

1. **Clone the repository**:
   ```bash
   git clone https://github.com/AngeloGabriel-dev/E-commerce-microsservices.git
   cd E-commerce-microsservices
   ```

2. **Configure environment variables** (optional):
   ```bash
   # For email notifications (Notification Service)
   export MAIL_USERNAME=your-email@gmail.com
   export MAIL_PASSWORD=your-app-password
   ```

3. **Start all services**:
   ```bash
   docker-compose up --build
   ```

4. **Access the services**:
   - Frontend: [http://localhost:3000](http://localhost:3000)
   - API Gateway: [http://localhost:8080](http://localhost:8080)
   - Auth Service Swagger: [http://localhost:8080/auth-swagger/swagger-ui.html](http://localhost:8080/auth-swagger/swagger-ui.html)
   - Catalog Service Swagger: [http://localhost:8080/catalog-swagger/swagger-ui.html](http://localhost:8080/catalog-swagger/swagger-ui.html)

### Running Locally (Development)

Each service can be run independently for development. You'll need PostgreSQL and Kafka running locally (or via Docker).

1. **Start infrastructure**:
   ```bash
   docker-compose up postgres kafka
   ```

2. **Build the shared library**:
   ```bash
   cd ecommerce-common
   mvn clean install
   ```

3. **Run a specific service**:
   ```bash
   cd Auth-Service
   mvn spring-boot:run
   ```

4. **Run the frontend**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

---
## Application Screens (`application-screens`)
### Start Screen
![Screenshot](https://github.com/AngeloGabriel-dev/E-commerce-microsservices/blob/main/images%20for%20presentation/Start-Screen.png)
### Car Screen
![Screenshot](https://github.com/AngeloGabriel-dev/E-commerce-microsservices/blob/main/images%20for%20presentation/Car-Screen.png)
### Checkout Screen
![Screenshot](https://github.com/AngeloGabriel-dev/E-commerce-microsservices/blob/main/images%20for%20presentation/Checkout-Screen.png)
### Register Screen
![Screenshot](https://github.com/AngeloGabriel-dev/E-commerce-microsservices/blob/main/images%20for%20presentation/Register-Screen.png)
### Login Screen
![Screenshot](https://github.com/AngeloGabriel-dev/E-commerce-microsservices/blob/main/images%20for%20presentation/Login-Screen.png)
### Create Product Screen
![Screenshot](https://github.com/AngeloGabriel-dev/E-commerce-microsservices/blob/main/images%20for%20presentation/CreateProduct-Screen.png)
### Catalog Screen
![Screenshot](https://github.com/AngeloGabriel-dev/E-commerce-microsservices/blob/main/images%20for%20presentation/Catalog-Screen.png)
### Product Screen
![Screenshot](https://github.com/AngeloGabriel-dev/E-commerce-microsservices/blob/main/images%20for%20presentation/Product-Screen.png)

## Justification of Technical Choices

### Why Microservices?

The decision to use microservices instead of a monolithic architecture was driven by the need for:
- **Scalability**: Each service can be scaled independently based on demand. During peak shopping seasons, the Order and Payment services can be scaled without affecting the Catalog or User services.
- **Fault Isolation**: A failure in the Payment Service does not crash the Catalog Service. Services degrade gracefully.
- **Independent Deployments**: Teams can deploy changes to individual services without redeploying the entire application.
- **Technology Flexibility**: Each service could theoretically use different technologies if needed.

### Why Kafka for Event-Driven Communication?

- **Reliability**: Kafka persists messages to disk and supports replication, ensuring no events are lost even if consumers are down. If the Notification Service is temporarily unavailable, it can replay the events when it comes back online.
- **Decoupling**: The Order Service does not need to know about the Notification Service. It simply publishes an event, and any interested service can consume it.
- **At-Least-Once Delivery**: Kafka guarantees that messages are delivered at least once, which is essential for payment workflows where every event must be processed.
- **Ordering Guarantees**: Messages within a partition are ordered, ensuring that state transitions happen in the correct sequence (e.g., `payment-confirmed` is not processed before `payment-created`).

### Why REST for Synchronous Operations?

While Kafka excels at event-driven workflows, synchronous REST is used where:
- **Immediate Response is Required**: When creating an order, the system needs to fetch product data (price, seller ID, stock) immediately. Kafka's asynchronous nature would introduce unacceptable latency.
- **Request-Response Semantics**: Operations like user login, registration, and product listing follow a natural request-response pattern.
- **Simplicity**: For CRUD operations, REST is simpler and more intuitive than building complex request/reply patterns over Kafka.

### Why PostgreSQL?

- **Maturity and Reliability**: PostgreSQL is a battle-tested, production-grade database with ACID compliance.
- **JSONB Support**: The Product entity uses JSONB columns for flexible attributes and image URLs, avoiding the need for a separate NoSQL database for this use case.
- **Per-Service Databases**: PostgreSQL's support for multiple databases on a single server makes it easy to implement the database-per-service pattern in Docker Compose.
- **Tooling and Community**: Extensive ecosystem, excellent documentation, and strong community support.

### Why Spring Cloud Gateway?

- **Reactive Architecture**: Built on Spring WebFlux, providing non-blocking I/O suitable for high-throughput routing.
- **Declarative Route Configuration**: Routes are defined in YAML, making it easy to add/modify service routes.
- **Path Rewriting**: Swagger routes are rewritten to expose each service's API documentation through the gateway.
- **Seamless Integration**: Works natively with the Spring Boot ecosystem used across all services.

### Why Java 21 + Spring Boot 3.5?

- **Virtual Threads (Project Loom)**: Java 21 introduces virtual threads, allowing highly concurrent applications with simpler thread-per-request models.
- **Record Types**: Used extensively in the codebase for DTOs and Kafka events, reducing boilerplate.
- **Spring Boot 3.5**: Latest stable version with improved security, observability, and performance.
- **Team Familiarity**: The team has strong expertise in the Java/Spring ecosystem.

### Why Separated Auth and User Services?

While similar, these services have distinct concerns:
- **Auth Service**: Handles security-sensitive operations (password hashing, JWT generation, service account management). It has a minimal user entity (email, password, role).
- **User Service**: Manages business-related profile data (name, CPF, phone). It consumes events from Auth Service for synchronization.

This separation ensures that security credentials are isolated from business data, and the User Service can evolve independently without affecting authentication flows.

### Why Multi-Seller Order Model?

The order model supports a **marketplace** pattern where a single order can contain products from multiple sellers. This is achieved through a three-level hierarchy: `Order → SellerOrder → OrderItem`. Each seller order has its own subtotal and status, enabling per-seller fulfillment while maintaining a unified checkout experience for the customer.

---

## License

This project is licensed under the MIT License.

---

*Built with ❤️ by [AngeloGabriel-dev](https://github.com/AngeloGabriel-dev)*
