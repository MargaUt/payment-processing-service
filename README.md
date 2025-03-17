# Payment Service

## Overview
This is a RESTful web service for payment processing. The application allows clients to create, cancel, and query payments while ensuring efficient processing and logging of client country information.

## Features
- **Payment Creation**: Supports three payment types (TYPE1, TYPE2, TYPE3) with mandatory and optional fields.
- **Payment Cancellation**: Allows cancellation on the day of creation before 00:00, with a calculated cancellation fee.
- **Payments Querying**: Retrieves non-canceled payments and allows filtering by amount.
- **Client Country Logging**: Logs client country based on clients IP.
- **Notification Service**: Notifies external services about valid TYPE1 and TYPE2 payments.

## Payment Types
- **TYPE1**: Only applicable for EUR payments, requires a mandatory `details` field.
- **TYPE2**: Only applicable for USD payments, has an optional `details` field.
- **TYPE3**: Applicable for both EUR and USD, requires a `creditorBIC` field.

## Cancellation Fee Calculation
Cancellation fees are calculated as:
\[ fee = hours * coefficient \]
- **TYPE1**: Coefficient = 0.05
- **TYPE2**: Coefficient = 0.1
- **TYPE3**: Coefficient = 0.15

## API Endpoints
### 1. Create Payment
```
POST /payments
Content-Type: application/json
{
  "amount": 1222,
  "currency": "USD",
  "debtorIban": "DE8937040044053",
  "creditorIban": "GB29NWBK601613319"
}
```

### 2. Cancel Payment
```
POST /payments/{paymentId}/cancel
```

### 3. Query Payments
- **Get all active payments**
```
GET /payments
```
- **Get specific payment details**
```
GET /payments/{paymentId}
```

## Technologies Used
- **Spring Boot 3.2.2** (REST API, Validation, Logging)
- **H2 Database** (In-memory for testing)
- **Spring Data JPA** (Persistence Layer)
- **Spring Boot Starter Test** (Unit & Integration Testing)
- **ModelMapper** (DTO to Entity Mapping)
- **Springdoc OpenAPI** (API Documentation)

## Running the Application
1. Clone the repository:
   ```
   git clone https://github.com/example/payment-service.git
   ```
2. Navigate to the project directory:
   ```
   cd payment-service
   ```
3. Build and run the application using Maven:
   ```
   mvn spring-boot:run
   ```
4. Access the API documentation:
   ```
   http://localhost:8080/swagger-ui.html
   ```
