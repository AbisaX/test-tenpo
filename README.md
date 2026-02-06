**Autor:** Abisaid Gomez
**Repositorio:** https://github.com/AbisaX/test-tenpo.git


# API Calculadora - Desafío Tenpo

API REST desarrollada en Spring Boot con Java 21 que implementa un servicio de cálculo con porcentaje dinámico, siguiendo arquitectura hexagonal y programación reactiva.

**Creado por:** Abisaid Gomez

---

## Tabla de Contenidos

- [Características](#características)
- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Requisitos Previos](#requisitos-previos)
- [Despliegue Local](#despliegue-local)
- [Endpoints](#endpoints)
- [Testing](#testing)
- [Justificación Técnica](#justificación-técnica)

---

## Características

- **Cálculo con porcentaje dinámico**: Suma dos números y aplica un porcentaje obtenido de un servicio externo (mock retorna 10%)
- **Reintentos automáticos**: Lógica de retry con hasta 3 intentos usando Resilience4j
- **Historial de llamadas**: Registro asíncrono de todas las llamadas con paginación
- **Rate Limiting**: Máximo 3 peticiones por minuto usando algoritmo Token Bucket
- **Manejo de errores HTTP**: Respuestas descriptivas en español para errores 4XX y 5XX
- **Documentación OpenAPI/Swagger**: Documentación interactiva completamente en español

---

## Arquitectura

El proyecto implementa **Arquitectura Hexagonal** (Ports & Adapters):

```
src/main/java/com/tenpo/calculator/
├── domain/                          # Núcleo de negocio
│   ├── model/                       # Entidades de dominio
│   │   ├── CalculationResult.java
│   │   └── CallHistory.java
│   ├── exception/                   # Excepciones de dominio
│   │   ├── ExternalServiceException.java
│   │   └── RateLimitExceededException.java
│   └── port/                        # Puertos (interfaces)
│       ├── input/                   # Casos de uso
│       │   ├── CalculatorUseCase.java
│       │   └── CallHistoryUseCase.java
│       └── output/                  # Puertos de salida
│           ├── PercentageServicePort.java
│           └── CallHistoryRepositoryPort.java
├── application/                     # Capa de aplicación
│   └── service/                     # Implementación de casos de uso
│       ├── CalculatorService.java
│       └── CallHistoryService.java
└── infrastructure/                  # Capa de infraestructura
    ├── adapter/
    │   ├── input/rest/              # Adaptadores REST (Controllers)
    │   └── output/
    │       ├── external/            # Cliente servicio externo
    │       └── persistence/         # Adaptador de persistencia
    ├── config/                      # Configuraciones
    ├── filter/                      # WebFilters (Rate Limiting)
    └── exception/                   # Manejadores de excepciones
```

---

## Tecnologías

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.2.1 | Framework base |
| Spring WebFlux | - | Programación reactiva |
| Spring Data R2DBC | - | Acceso reactivo a datos |
| PostgreSQL | 16 | Base de datos |
| Flyway | 10.4.1 | Migraciones de BD |
| Resilience4j | 2.2.0 | Patrón de reintentos |
| Bucket4j | 8.7.0 | Rate limiting |
| SpringDoc OpenAPI | 2.3.0 | Documentación API |
| JaCoCo | 0.8.11 | Cobertura de código |
| Testcontainers | 1.19.3 | Tests de integración |
| Docker | - | Containerización |
| WireMock | - | Mock de servicios externos |

---

## Requisitos Previos

| Requisito | Versión | Notas |
|-----------|---------|-------|
| Java | 21 | JDK Corretto, OpenJDK o similar |
| Maven | 3.9+ | Incluido en el proyecto (`apache-maven-3.9.6/`) |
| Docker Desktop | 28+ | Para ejecutar con contenedores |
| Git | - | Para clonar el repositorio |

---

## Despliegue Local

### Clonar el Repositorio

```bash
git clone https://github.com/AbisaX/test-tenpo.git
cd test-tenpo
```

### Opción 1: Docker Compose (Recomendado)

```bash
# Construir y levantar todos los servicios
docker-compose up --build
```

Esto levantará:
- **PostgreSQL** en puerto 5432
- **WireMock** (mock del servicio externo) en puerto 8081
- **API Calculadora** en puerto 8080

### Opción 2: Ejecución Local con Maven

**Paso 1:** Levantar PostgreSQL con Docker:
```bash
docker run -d --name postgres-calculator ^
  -e POSTGRES_DB=calculator_db ^
  -e POSTGRES_USER=postgres ^
  -e POSTGRES_PASSWORD=postgres ^
  -p 5432:5432 ^
  postgres:16-alpine
```

**Paso 2:** Ejecutar la aplicación:
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Opción 3: JAR Ejecutable

```bash
# Compilar
mvnw.cmd clean package -DskipTests

# Ejecutar
java -jar target/calculator-api-1.0.0.jar
```

---

## Endpoints

### URLs de Acceso

| Recurso | URL |
|---------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **API Docs (JSON)** | http://localhost:8080/api-docs |
| **Health Check** | http://localhost:8080/actuator/health |

### Endpoints de la API

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/calculator/calculate` | Calcula suma con porcentaje |
| GET | `/api/v1/history` | Obtiene historial paginado |

### Ejemplos de Uso

**Calcular suma con porcentaje:**
```bash
curl -X POST http://localhost:8080/api/v1/calculator/calculate ^
  -H "Content-Type: application/json" ^
  -d "{\"num1\": 5, \"num2\": 5}"
```

**Respuesta exitosa (200 OK):**
```json
{
  "num1": 5,
  "num2": 5,
  "sum": 10,
  "percentageApplied": 10,
  "result": 11
}
```

**Obtener historial:**
```bash
curl "http://localhost:8080/api/v1/history?page=0&size=10"
```

**Respuesta:**
```json
{
  "content": [
    {
      "id": 1,
      "timestamp": "2024-01-15T10:30:00",
      "endpoint": "/api/v1/calculator/calculate",
      "httpMethod": "POST",
      "parameters": "{\"num1\": 5, \"num2\": 5}",
      "response": "{\"result\": 11}",
      "statusCode": 200,
      "success": true
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

---

## Estructura de Errores

Todos los mensajes de error están en español:

| Código | Error | Mensaje |
|--------|-------|---------|
| 400 | Solicitud Incorrecta | `num1 es requerido` / `num2 es requerido` |
| 429 | Demasiadas Peticiones | `Límite de peticiones excedido. Máximo 3 peticiones por minuto permitidas.` |
| 503 | Servicio No Disponible | `El servicio externo de porcentaje no está disponible después de 3 intentos.` |
| 500 | Error Interno del Servidor | `Ocurrió un error inesperado. Por favor, intente nuevamente más tarde.` |

**Ejemplo de respuesta de error:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 429,
  "error": "Demasiadas Peticiones",
  "message": "Límite de peticiones excedido. Máximo 3 peticiones por minuto permitidas. Por favor, intente nuevamente más tarde.",
  "path": "/api/v1/calculator/calculate"
}
```

---

## Testing

### Ejecutar Tests Unitarios
```bash
mvnw.cmd test
```

### Ejecutar Tests sin Integración (más rápido)
```bash
mvnw.cmd test -Dtest="!*IntegrationTest"
```

### Ejecutar Tests con Cobertura
```bash
mvnw.cmd verify
```

El reporte de cobertura se genera en `target/site/jacoco/index.html`

### Estadísticas de Tests

| Tipo | Cantidad |
|------|----------|
| Tests Unitarios | 48 |
| Tests de Integración | 9 |
| **Total** | **57** |
| Cobertura Objetivo | >80% |


## Justificación Técnica

### 1. Spring WebFlux (Programación Reactiva)

Se utiliza Spring WebFlux porque permite que la aplicación atienda muchas solicitudes simultáneas sin quedarse esperando. En el modelo tradicional, cada petición ocupa un hilo mientras espera respuestas de la base de datos o servicios externos, lo que limita cuántos usuarios puede atender el servidor al mismo tiempo. Con WebFlux, el sistema no se queda bloqueado esperando; mientras una operación está en proceso, puede atender otras solicitudes.

Es como un restaurante donde el mesero tradicional toma la orden, va a la cocina, espera a que esté lista y la trae antes de atender a otra mesa. Con el enfoque reactivo, el mesero toma la orden, la pasa a la cocina, y mientras se prepara, atiende otras mesas. Cuando el plato está listo, lo recoge y lo lleva. El mismo mesero puede atender muchas más mesas porque no pierde tiempo esperando.

### 2. Arquitectura Hexagonal

Se implementa la arquitectura hexagonal para separar claramente la lógica de negocio de los detalles técnicos como la base de datos o los controladores REST. El núcleo de la aplicación, donde viven las reglas de cálculo, no sabe ni le importa si los datos vienen de PostgreSQL, MongoDB o un archivo. Esta separación hace que el código sea más fácil de probar y modificar.

Es como una empresa donde el gerente de operaciones se enfoca solo en las decisiones del negocio y tiene asistentes que se encargan de hablar con proveedores, clientes y bancos. Si mañana la empresa cambia de banco, el gerente no tiene que aprender nada nuevo porque su asistente se encarga de esa comunicación. El gerente sigue tomando las mismas decisiones de negocio sin importar qué banco use la empresa.

### 3. R2DBC sobre JDBC

Se opta por R2DBC en lugar del tradicional JDBC para mantener la consistencia del modelo reactivo en toda la aplicación. Si se usara JDBC con WebFlux, cada consulta a la base de datos bloquearía el hilo, desperdiciando las ventajas del enfoque reactivo. R2DBC permite que las operaciones de base de datos también sean no bloqueantes, manteniendo el flujo eficiente de principio a fin.

Es como una cadena de producción donde todas las máquinas trabajan de forma continua excepto una que se detiene completamente mientras procesa cada pieza. Esa máquina lenta se convierte en un cuello de botella que frena toda la línea. R2DBC evita ese cuello de botella haciendo que la comunicación con la base de datos fluya al mismo ritmo que el resto del sistema.

### 4. Bucket4j para Rate Limiting

Se implementa el límite de peticiones con Bucket4j usando el algoritmo Token Bucket porque ofrece un balance perfecto entre protección y flexibilidad. Este algoritmo acumula tokens con el tiempo y cada petición consume uno. Si llegan muchas peticiones de golpe, se atienden mientras haya tokens disponibles, pero una vez agotados, las siguientes deben esperar.

Funciona como el sistema de fichas de una lavandería automática. Cada hora entregan 3 fichas y cada lavado consume una. Si un cliente llega con mucha ropa sucia, puede usar sus 3 fichas de una vez, pero después debe esperar a que le den más. Esto evita que un solo cliente acapare todas las máquinas todo el día, permitiendo que otros también puedan usar el servicio.

### 5. Resilience4j para Reintentos

Se utiliza Resilience4j para manejar los reintentos cuando el servicio externo falla porque ofrece reintentos inteligentes con espera incremental. En lugar de bombardear un servicio caído con peticiones inmediatas, el sistema espera 1 segundo antes del primer reintento, 2 segundos antes del segundo, y 4 segundos antes del tercero. Esto le da tiempo al servicio externo para recuperarse.

Es como cuando alguien llama a otra persona y no contesta. No tiene sentido marcar inmediatamente una y otra vez porque probablemente sigue ocupado. Es más efectivo esperar un poco, volver a intentar, y si no contesta, esperar un poco más antes del siguiente intento. Así aumentan las probabilidades de que la persona ya esté disponible en el siguiente intento.

### 6. Registro Asíncrono de Historial

Se guarda el historial de llamadas de forma asíncrona para que el usuario reciba su respuesta lo más rápido posible. El guardado del historial sucede en segundo plano después de enviar la respuesta. Si por alguna razón falla el guardado, el usuario ya tiene su resultado y no se ve afectado.

Es como cuando un cliente paga en una tienda y el cajero le da el recibo inmediatamente. Mientras el cliente se va con su compra, el sistema registra la venta en el inventario, actualiza las estadísticas y genera reportes. El cliente no tiene que esperar a que todo eso termine para irse con sus productos. Si hay un problema guardando el registro, eso no afecta el hecho de que ya pagó y tiene su mercancía.

### 7. PostgreSQL como Base de Datos

Se selecciona PostgreSQL porque es una base de datos madura, confiable y con excelente soporte para operaciones reactivas a través de su driver R2DBC. Además, usando Flyway para las migraciones, se garantiza que el esquema de la base de datos sea consistente en todos los ambientes, desde el desarrollo local hasta producción.

Es como elegir un banco reconocido para guardar dinero en lugar de uno nuevo sin historial. PostgreSQL tiene décadas de uso en sistemas críticos de empresas grandes, lo que genera confianza en su estabilidad. Y Flyway actúa como un contador que mantiene los libros organizados, asegurando que las cuentas cuadren igual en todas las sucursales del banco.

---

## Colección Postman

Importa esta colección en Postman para probar la API:

```json
{
  "info": {
    "name": "API Calculadora - Tenpo",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Calcular con Porcentaje",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\"num1\": 5, \"num2\": 5}"
        },
        "url": {"raw": "http://localhost:8080/api/v1/calculator/calculate"}
      }
    },
    {
      "name": "Ver Historial",
      "request": {
        "method": "GET",
        "url": {"raw": "http://localhost:8080/api/v1/history?page=0&size=10"}
      }
    },
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "url": {"raw": "http://localhost:8080/actuator/health"}
      }
    }
  ]
}
```

---

## Variables de Entorno

| Variable | Descripción | Default |
|----------|-------------|---------|
| `DB_HOST` | Host de PostgreSQL | localhost |
| `DB_PORT` | Puerto de PostgreSQL | 5432 |
| `DB_NAME` | Nombre de la base de datos | calculator_db |
| `DB_USERNAME` | Usuario de PostgreSQL | postgres |
| `DB_PASSWORD` | Contraseña de PostgreSQL | postgres |
| `SERVER_PORT` | Puerto del servidor | 8080 |
| `EXTERNAL_SERVICE_URL` | URL del servicio externo | http://localhost:8081 |
| `EXTERNAL_SERVICE_MOCK_ENABLED` | Habilitar mock del servicio | true |
| `RATE_LIMIT_RPM` | Peticiones por minuto | 3 |

---

## Estructura del Proyecto

```
test-tenpo/
├── src/
│   ├── main/
│   │   ├── java/com/tenpo/calculator/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   └── test/
│       └── java/com/tenpo/calculator/
├── mock/                    # Configuración WireMock
├── docker-compose.yml       # Orquestación Docker
├── Dockerfile               # Imagen de la API
├── pom.xml                  # Dependencias Maven
└── README.md                # Este archivo
```




