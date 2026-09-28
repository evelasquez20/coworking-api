# 🏢 Coworking Space Management API

API RESTful desarrollada en **Spring Boot 3** para la gestión integral de reservas de espacios de coworking, autenticación con **JWT**, control de concurrencia, resiliencia ante fallos, eventos de dominio y generación de reportes con caché.

---

## 🛠️ Tecnologías Utilizadas

- **Java 21** & **Spring Boot 3**
- **Spring Security** (Autenticación Stateless mediante JWT)
- **Spring Data JPA** & **PostgreSQL**
- **Resilience4j** (Circuit Breaker & Fallback)
- **Spring Cache** (Caché en memoria)
- **Spring Async & ApplicationEventPublisher** (Eventos de dominio y notificaciones)
- **OpenAPI 3 / Swagger UI** (Documentación interactiva)
- **Docker & Docker Compose** (Contenedor de base de datos)
- **JUnit 5 & Mockito** (Pruebas unitarias e integración)

---

## 🧠 Decisiones de Diseño y Patrones Aplicados (Requisitos de la Prueba)

Para cumplir con los criterios de evaluación técnicos, el sistema implementa las siguientes soluciones arquitectónicas:

### 1. Resiliencia con Circuit Breaker (Resilience4j)
Se integró `spring-cloud-starter-circuitbreaker-resilience4j` en el servicio de validación de pagos.
- **Problema resuelto:** Evitar que la lentitud o caída de la pasarela de pagos externa bloquee los hilos de nuestra API.
- **Fallback:** Si el circuito se abre (falla el servicio externo), el método de fallback captura la excepción y mantiene la reserva en estado `PENDING_PAYMENT` en lugar de devolver un error 500 al usuario, permitiendo reintentar el pago más tarde.

### 2. Eventos Asíncronos (Observer Pattern)
Se utilizó `ApplicationEventPublisher` junto con la anotación `@Async` para el envío de notificaciones.
- **Problema resuelto:** Desacoplar la lógica principal de negocio (confirmar reserva) de las tareas secundarias (enviar correo). 
- **Flujo:** Al confirmar una reserva, se emite un `ReservationConfirmedEvent`. Un *Listener* asíncrono lo captura y simula el envío del correo de confirmación. Esto evita sumar latencia a la respuesta HTTP que recibe el cliente.

### 3. Patrón de Estado (State Pattern)
Se aplicó para gestionar el ciclo de vida de la reserva (`PENDING_PAYMENT`, `CONFIRMED`, `COMPLETED`, `CANCELLED`).
- **Problema resuelto:** Evita estructuras complejas de `if/else` al intentar cancelar o pagar una reserva. El sistema valida automáticamente las transiciones de estado permitidas lanzando una excepción `BusinessException(INVALID_RESERVATION_STATE)` si se intenta una transición inválida.

### 4. Caché de Reportes (`@Cacheable`)
El endpoint de ocupación de espacios (`/reports/occupancy`) utiliza Spring Cache.
- **Justificación:** Los reportes de ocupación son consultas pesadas para la base de datos. Al cachear el resultado en memoria según los parámetros de fecha, se reduce drásticamente la carga sobre PostgreSQL en peticiones repetitivas.

---

## 💻 Instrucciones de Ejecución Local

### Prerrequisitos
- Docker y Docker Compose instalados.
- JDK 21 instalado.
- Maven 3.8+ (o el wrapper `./mvnw`).

### 1. Iniciar la Base de Datos con Docker
```bash
docker-compose up -d

```

### 2. Ejecutar la Aplicación

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

```

### 3. Ejecutar las Pruebas

```bash
./mvnw clean test

```

---

## 📑 Documentación de la API (Swagger UI)

Una vez iniciada la aplicación, accede a la documentación interactiva desde tu navegador:

* **Swagger UI:** `http://localhost:8080/swagger-ui.html`
* **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## 📌 Endpoints Principales

### Autenticación (`/api/v1/auth`)

| Método | Endpoint | Descripción | Body (JSON) |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Registro de nuevos usuarios | `{ "firstName": "...", "email": "...", "password": "...", "role": "USER/ADMIN" }` |
| `POST` | `/auth/login` | Inicio de sesión | `{ "email": "...", "password": "..." }` |

### Gestión de Espacios (`/api/v1/spaces`)

| Método | Endpoint | Descripción | Requiere |
| --- | --- | --- | --- |
| `GET` | `/spaces` | Listar todos los espacios | Autenticación |
| `GET` | `/spaces/{id}` | Obtener detalle de un espacio | Autenticación |
| `POST` | `/spaces` | Crear un nuevo espacio | Rol `ADMIN` |
| `PUT` | `/spaces/{id}` | Actualizar un espacio | Rol `ADMIN` |
| `DELETE` | `/spaces/{id}` | Eliminar un espacio | Rol `ADMIN` |

### Reservas (`/api/v1/reservations`)

| Método | Endpoint | Descripción | Body / Params |
| --- | --- | --- | --- |
| `GET` | `/reservations/my-reservations` | Ver reservas del usuario actual | N/A |
| `GET` | `/reservations` | Ver todas las reservas | Rol `ADMIN` |
| `POST` | `/reservations` | Crear reserva | `{ "spaceId": 1, "startTime": "...", "endTime": "..." }` |
| `POST` | `/reservations/{id}/pay` | Pagar reserva | `{ "paymentMethodId": "..." }` |
| `PATCH` | `/reservations/{id}/cancel` | Cancelar reserva | N/A |

### Reportes (`/api/v1/reports`)

| Método | Endpoint | Descripción | Query Params |
| --- | --- | --- | --- |
| `GET` | `/reports/occupancy` | Reporte de ocupación | `?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` (Requiere `ADMIN`) |
