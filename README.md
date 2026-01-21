# E-Commerce Microservices con RabbitMQ

Sistema de e-commerce basado en microservicios que implementa comunicacion asincrona mediante RabbitMQ para el procesamiento de pedidos.

## Arquitectura

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Cliente/App   │────▶│  Order Service  │────▶│    RabbitMQ     │
│                 │◀────│    (8080)       │◀────│   (15672/5672)  │
└─────────────────┘     └─────────────────┘     └────────┬────────┘
                                │                        │
                                ▼                        ▼
                        ┌─────────────────┐     ┌─────────────────┐
                        │   PostgreSQL    │     │Inventory Service│
                        │     (5432)      │◀────│    (8081)       │
                        └─────────────────┘     └─────────────────┘
```

## Tecnologias

- **Java 25**
- **Spring Boot 3.5.7**
- **PostgreSQL 15**
- **RabbitMQ 3**
- **Docker & Docker Compose**
- **Maven 3.9.6** (con Maven Wrapper incluido)

## Estructura del Proyecto

```
control-lectura/
├── order-service/                    # Microservicio de Pedidos (Puerto 8080)
│   ├── .mvn/wrapper/
│   ├── mvnw, mvnw.cmd
│   ├── pom.xml
│   └── src/main/java/ec/edu/espe/order_service/
│       ├── config/RabbitMQConfig.java
│       ├── controller/OrderController.java
│       ├── dto/
│       ├── messaging/
│       ├── model/
│       ├── repository/
│       └── service/
├── inventory-service/                # Microservicio de Inventario (Puerto 8081)
│   ├── .mvn/wrapper/
│   ├── mvnw, mvnw.cmd
│   ├── pom.xml
│   └── src/main/java/ec/edu/espe/inventory_service/
│       ├── config/
│       │   ├── RabbitMQConfig.java
│       │   └── DataLoader.java       # Carga productos automaticamente
│       ├── controller/InventoryController.java
│       ├── dto/
│       ├── messaging/
│       ├── model/
│       ├── repository/
│       └── service/
├── infrastructure/
│   ├── docker-compose.yml
│   └── .env.example
└── README.md
```

## Requisitos Previos

- **Docker** y **Docker Compose**
- **Java 25** (JDK 25)

---

## Instalacion y Ejecucion

### Paso 1: Iniciar Docker (PostgreSQL + RabbitMQ)

```bash
cd control-lectura/infrastructure
docker-compose up -d
```

### Paso 2: Crear las bases de datos

Ejecutar este comando para crear las dos bases de datos:

```bash
docker exec postgres-ecommerce psql -U postgres -c "CREATE DATABASE db_orders;"
docker exec postgres-ecommerce psql -U postgres -c "CREATE DATABASE db_inventory;"
```

### Paso 3: Verificar que todo esta corriendo

```bash
docker-compose ps
```

Resultado esperado:
```
NAME                 STATUS              PORTS
postgres-ecommerce   Up (healthy)        0.0.0.0:5432->5432/tcp
rabbitmq             Up (healthy)        0.0.0.0:5672->5672/tcp, 0.0.0.0:15672->15672/tcp
```

### Paso 4: Ejecutar Order Service

En una terminal:

```bash
cd control-lectura/order-service

# Windows:
mvnw.cmd spring-boot:run

# Linux/Mac:
./mvnw spring-boot:run
```

Disponible en: http://localhost:8080

### Paso 5: Ejecutar Inventory Service

En otra terminal:

```bash
cd control-lectura/inventory-service

# Windows:
mvnw.cmd spring-boot:run

# Linux/Mac:
./mvnw spring-boot:run
```

Disponible en: http://localhost:8081

### Paso 6: Acceder a RabbitMQ Management

URL: http://localhost:15672
- **Usuario:** admin
- **Password:** admin

---

## Datos de Prueba

El **Inventory Service** carga automaticamente estos productos al iniciar (via `DataLoader`):

| Product ID | Nombre | Stock |
|------------|--------|-------|
| `a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d` | Laptop HP Pavilion | 25 |
| `b7e8c9d1-2f3a-4b5c-8d9e-1a2b3c4d5e6f` | Mouse Logitech MX | 50 |
| `c1d2e3f4-5a6b-7c8d-9e0f-1a2b3c4d5e6f` | Keyboard Mechanical RGB | 30 |
| `d4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a` | Monitor Samsung 27 | 15 |
| `e5f6a7b8-9c0d-1e2f-3a4b-5c6d7e8f9a0b` | Webcam Logitech C920 | 2 |

---

## Endpoints API

### Order Service (Puerto 8080)

#### POST /api/v1/orders - Crear Pedido

```json
{
  "customerId": "9f7a1e2a-31f6-4a53-b0d2-6f4f1c7a3b2e",
  "items": [
    { "productId": "a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d", "quantity": 2 }
  ],
  "shippingAddress": {
    "country": "EC",
    "city": "Quito",
    "street": "Av. Amazonas",
    "postalCode": "170135"
  },
  "paymentReference": "pay_abc123"
}
```

**Respuesta (201):**
```json
{
  "orderId": "uuid-generado",
  "status": "PENDING",
  "message": "Order received. Inventory check in progress."
}
```

#### GET /api/v1/orders/{orderId} - Consultar Pedido

**Estados posibles:**
- `PENDING` - Verificando inventario
- `CONFIRMED` - Pedido confirmado, stock reservado
- `CANCELLED` - Cancelado por falta de stock

### Inventory Service (Puerto 8081)

#### GET /api/v1/products/{productId}/stock

```json
{
  "productId": "a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d",
  "availableStock": 25,
  "reservedStock": 0,
  "updatedAt": "2026-01-21T12:00:00Z"
}
```

---

## Flujo de Eventos (RabbitMQ)

```
1. Cliente ──POST /orders──▶ Order Service
2. Order Service: Guarda pedido (PENDING)
3. Order Service ──OrderCreated──▶ RabbitMQ
4. RabbitMQ ──────────────────────▶ Inventory Service
5. Inventory Service: Verifica stock
6a. Stock OK → StockReserved → Order CONFIRMED
6b. Sin stock → StockRejected → Order CANCELLED
```

**Exchange:** `orders.exchange` (Topic)
**Colas:** `order.created.queue`, `stock.response.queue`

---

## Pruebas con cURL

### Crear pedido (stock suficiente)
```bash
curl -X POST http://localhost:8080/api/v1/orders -H "Content-Type: application/json" -d "{\"customerId\":\"9f7a1e2a-31f6-4a53-b0d2-6f4f1c7a3b2e\",\"items\":[{\"productId\":\"a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d\",\"quantity\":2}],\"paymentReference\":\"pay_test\"}"
```

### Crear pedido (sin stock - Webcam solo tiene 2)
```bash
curl -X POST http://localhost:8080/api/v1/orders -H "Content-Type: application/json" -d "{\"customerId\":\"9f7a1e2a-31f6-4a53-b0d2-6f4f1c7a3b2e\",\"items\":[{\"productId\":\"e5f6a7b8-9c0d-1e2f-3a4b-5c6d7e8f9a0b\",\"quantity\":100}],\"paymentReference\":\"pay_fail\"}"
```

### Consultar pedido
```bash
curl http://localhost:8080/api/v1/orders/{orderId}
```

### Consultar stock
```bash
curl http://localhost:8081/api/v1/products/a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d/stock
```

---

## Comandos Docker Utiles

```bash
# Ver estado
docker-compose ps

# Ver logs
docker-compose logs -f

# Ver productos en BD
docker exec postgres-ecommerce psql -U postgres -d db_inventory -c "SELECT id, name, available_stock FROM products_stock;"

# Ver ordenes en BD
docker exec postgres-ecommerce psql -U postgres -d db_orders -c "SELECT id, status, reason FROM orders;"

# Reiniciar todo (borrar datos)
docker-compose down -v
docker-compose up -d
# Luego crear BDs de nuevo (Paso 2)
```

---

## Base de Datos

### db_orders
- **orders**: id, customer_id, status, reason, shipping info, timestamps
- **order_items**: id, order_id, product_id, quantity

### db_inventory
- **products_stock**: id, name, available_stock, reserved_stock, updated_at

---

## Principios SOLID

| Principio | Aplicacion |
|-----------|------------|
| **S** | Controllers, Services, Repositories separados |
| **O** | Interfaces para extensibilidad |
| **L** | Implementaciones sustituyen interfaces |
| **I** | Interfaces especificas por servicio |
| **D** | Inyeccion via `@RequiredArgsConstructor` |

---

## Autor

**Universidad de las Fuerzas Armadas ESPE**
Carrera de Software - Aplicaciones Distribuidas
2026
