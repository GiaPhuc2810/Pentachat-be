# 💼 Project Gauge - Spring Boot RESTful API

A production-ready Spring Boot RESTful API demonstrating clean layered architecture with user authentication and wallet management system.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Documentation](#documentation)
- [Architecture Highlights](#architecture-highlights)
- [Development Guide](#development-guide)

---

## 🎯 Overview

This project showcases best practices in Spring Boot application design with:

- **Clean Layered Architecture**: Controller → Service → Data Access
- **SOLID Principles**: Especially Dependency Inversion and Single Responsibility
- **Abstraction**: DataApi interface makes code database-agnostic
- **Mock Data**: In-memory storage for development, easily replaceable with JPA/Database
- **Production-Ready**: Error handling, validation, consistent responses

Perfect for learning backend development or as a template for new projects!

---

## ✨ Features

### Authentication
✅ User registration with validation  
✅ User login with credential checking  
✅ User ID-based authorization  

### Wallet System
✅ Deposit money  
✅ Withdraw money with balance checks  
✅ Transfer between users  
✅ Check balance  
✅ View transaction history  

### Technical Features
✅ Input validation (Jakarta validation)  
✅ Global exception handling  
✅ Consistent API response format  
✅ Mock data with easy database migration path  
✅ Logging-ready structure  

---

## 🛠 Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Java | 21 |
| Framework | Spring Boot | 3.5.10 |
| Build Tool | Maven | 3.6+ |
| HTTP | Spring Web | Latest |
| Validation | Jakarta Validation | Latest |
| Lombok | Code Generation | Latest |
| Testing | Spring Boot Test | Latest |

---

## 📁 Project Structure

```
demo/
├── src/main/java/com/example/demo/
│   ├── controller/
│   │   ├── AuthController.java              # Auth endpoints
│   │   └── WalletController.java            # Wallet endpoints
│   ├── service/
│   │   ├── AuthService.java                 # Auth business logic
│   │   └── WalletService.java               # Wallet business logic
│   ├─ repository/
│       ├─ TransactionRepository.class
│       ├─ UserRepository.class
│       └─ WalletRepository.class
│   ├── dataaccess/
│   │   ├── DataApi.java                     # Data interface (abstraction)
│   │   └── MockDataApiImpl.java             # Mock implementation
|   |   └── JpaDataApiImpl.java              # Jpa Implementation
│   ├── datastore/
│   │   └── MockDataStore.java               # In-memory database
│   ├── model/
│   │   ├── User.java                        # User entity
│   │   ├── Wallet.java                      # Wallet entity
│   │   └── Transaction.java                 # Transaction entity
│   ├── dto/
│   │   ├── request/                         # Request DTOs
│   │   │   ├── RegisterRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── DepositRequest.java
│   │   │   ├── WithdrawRequest.java
│   │   │   └── TransferRequest.java
│   │   └── response/                        # Response DTOs
│   │       ├── AuthResponse.java
│   │       ├── BalanceResponse.java
│   │       ├── TransactionResponse.java
│   │       └── ApiResponse.java
│   ├── exception/
│   │   ├── AppException.java                # Custom exception
│   │   └── GlobalExceptionHandler.java      # Centralized error handling
│   ├── util/
│   │   └── IdGenerator.java                 # UUID generation
│   └── ProjectGaugeApplication.java         # Main Spring Boot class
├── src/test/java/                           # Test classes
├── pom.xml                                  # Maven dependencies
├── README.md                                # This file
├── API_DOCUMENTATION.md                     # Complete API docs
├── QUICKSTART.md                            # Quick start guide
├── ARCHITECTURE.md                          # Architecture details
└── CODE_EXAMPLES.md                         # Code usage examples
```

---

## 🚀 Getting Started

### Prerequisites
- Java 21 (JDK)
- Maven 3.6+
- Git (optional)

### Installation

1. **Clone or navigate to the project**
```bash
cd d:\softwave\KiemThu\software
```

2. **Build the project**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

4. **Access the API**
```
http://localhost:8080
```

---

## 📊 API Endpoints

### Authentication
```
POST   /auth/register          Register new user
POST   /auth/login             Login user
```

### Wallet Management
```
GET    /wallet/balance         Get wallet balance
POST   /wallet/deposit         Deposit money
POST   /wallet/withdraw        Withdraw money
POST   /wallet/transfer        Transfer to another user
GET    /wallet/transactions    Get transaction history
```

**Full API documentation**: See [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

---

## 📚 Documentation

This project includes comprehensive documentation:

| Document | Purpose |
|----------|---------|
| [API_DOCUMENTATION.md](API_DOCUMENTATION.md) | Complete API reference with examples |
| [QUICKSTART.md](QUICKSTART.md) | Quick start guide and workflows |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Detailed architecture documentation |
| [CODE_EXAMPLES.md](CODE_EXAMPLES.md) | Real code usage examples |
| [QuyUoc.md](QuyUoc.md) | Vietnamese coding conventions |

---

## 🏗️ Architecture Highlights

### Layered Architecture
```
HTTP Request
    ↓
Controller (HTTP handling)
    ↓
Service (Business logic)
    ↓
DataApi Interface (Abstraction)
    ↓
Implementation (Mock or Database)
    ↓
Data Store
```

### Key Design Patterns

**1. Dependency Injection**
- Spring manages all dependencies
- Easy to test with mocks

**2. Interface Abstraction**
- Services depend on DataApi interface
- Easy to swap implementations

**3. Data Transfer Objects (DTOs)**
- Request/Response separation
- Validation at entry point

**4. Global Exception Handling**
- Centralized error handling
- Consistent error responses

**5. Single Responsibility**
- Each class has one reason to change
- Easy to understand and maintain

### Easy Database Migration

**Current:** Mock implementation  
**Future:** Replace MockDataApiImpl with JpaDataApiImpl

**No changes needed** in Controller or Service layers!

---

## 💻 Development Guide

### Adding a New Feature

1. **Update models** (`model/`)
2. **Create DTOs** (`dto/request/`, `dto/response/`)
3. **Add DataApi methods** (`dataaccess/DataApi.java`)
4. **Implement DataApi** (`dataaccess/MockDataApiImpl.java`)
5. **Add service methods** (`service/`)
6. **Add controller endpoints** (`controller/`)

### Project Conventions

Following [QuyUoc.md](QuyUoc.md):

- **camelCase**: Variables and methods
- **PascalCase**: Classes and interfaces
- **SCREAMING_SNAKE_CASE**: Constants
- **4 spaces**: Indentation
- **DRY**: Don't Repeat Yourself
- **Early return**: Fail fast

---

## 🧪 Testing

### Unit Testing
```bash
mvn test
```

### Integration Testing
```bash
mvn verify
```

### Manual Testing
See [QUICKSTART.md](QUICKSTART.md) for curl examples

---

## 📝 Mock Data

Pre-loaded users for testing:

| Username | Password | Balance |
|----------|----------|---------|
| alice | password123 | $1000 |
| bob | password456 | $500 |

You can login immediately:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}'
```

---

## 🔍 Code Quality

### Features
✅ Clean code with meaningful names  
✅ Comprehensive error handling  
✅ Input validation  
✅ Consistent response format  
✅ Well-documented code  
✅ Follows Spring Boot best practices  

### Error Handling
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

---

## 🎓 Learning Objectives

This project is designed to teach:

1. **Spring Boot Basics**
   - Application setup and configuration
   - Dependency injection
   - Controllers and services

2. **REST API Design**
   - RESTful principles
   - HTTP methods and status codes
   - JSON request/response

3. **Layered Architecture**
   - Separation of concerns
   - Dependency inversion principle
   - SOLID principles

4. **Design Patterns**
   - Data Access Object (DAO) pattern
   - Service locator pattern
   - Factory pattern (for DTOs)

5. **Best Practices**
   - Input validation
   - Error handling
   - Consistent naming conventions

---

## 🚀 Next Steps

### Enhance the Project

- [ ] Add Spring Security with JWT tokens
- [ ] Implement password hashing (bcrypt)
- [ ] Add database support (PostgreSQL + JPA)
- [ ] Write comprehensive unit tests
- [ ] Add Swagger/OpenAPI documentation
- [ ] Add logging (SLF4J + Logback)
- [ ] Add rate limiting
- [ ] Add caching (Redis)
- [ ] Add transaction approval workflow
- [ ] Add email notifications

### Production Deployment

1. **Add database** - PostgreSQL/MySQL
2. **Add security** - Spring Security + JWT
3. **Add monitoring** - Spring Actuator + Prometheus
4. **Add logging** - ELK stack
5. **Containerize** - Docker
6. **Deploy** - Kubernetes/Cloud

---

## 📞 Support

### Troubleshooting

**Port already in use:**
```bash
# Change port in application.properties
server.port=8081
```

**Compilation errors:**
```bash
# Clean and rebuild
mvn clean compile
```

**Dependency issues:**
```bash
# Update dependencies
mvn clean install -U
```

---

## 📜 License

This project is provided as-is for educational purposes.

---

## 🙏 Credits

Built with ❤️ as a learning project demonstrating Spring Boot best practices.

**Technologies Used:**
- Spring Boot 3.5.10
- Java 21
- Maven
- Lombok
- Jakarta Validation

---

## 🎯 Quick Links

- [API Documentation](API_DOCUMENTATION.md)
- [Quick Start Guide](QUICKSTART.md)
- [Architecture Details](ARCHITECTURE.md)
- [Code Examples](CODE_EXAMPLES.md)
- [Coding Conventions](QuyUoc.md)

---

**Start coding! The API is ready to use. 🚀**

For detailed API usage, see [API_DOCUMENTATION.md](API_DOCUMENTATION.md)  
For step-by-step examples, see [QUICKSTART.md](QUICKSTART.md)  
For architecture deep dive, see [ARCHITECTURE.md](ARCHITECTURE.md)
