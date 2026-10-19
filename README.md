# Prices service

![CI](https://github.com/kardos24/DemoPrecios/actions/workflows/ci.yml/badge.svg)

Servicio REST (Spring Boot) que devuelve el precio aplicable a un producto de una cadena en una fecha
dada. Si varias tarifas coinciden en esa fecha se aplica la de mayor prioridad.

## Cómo probar el proyecto desde cero

### 1. Requisitos

- **Git**
- **Java 17 o superior**. Comprueba la versión con:

  ```bash
  java -version
  ```

No hace falta instalar Maven: el proyecto incluye el Maven Wrapper (`mvnw`), que descarga la versión
adecuada. La primera ejecución necesita conexión a internet para bajar Maven y las dependencias.

### 2. Descargar el proyecto

```bash
git clone https://github.com/kardos24/DemoPrecios.git
cd DemoPrecios
```

### 3. Compilar y ejecutar los tests

```bash
./mvnw clean verify          # Linux / macOS
.\mvnw.cmd clean verify      # Windows (PowerShell)
```

Debe terminar con `BUILD SUCCESS`. Durante el build se genera el código del contrato OpenAPI
(`PricesApi` y `PriceResponse`) en `target/generated-sources`; no hay que hacer nada a mano.

### 4. Arrancar la aplicación

```bash
./mvnw spring-boot:run       # Linux / macOS
.\mvnw.cmd spring-boot:run   # Windows (PowerShell)
```

Espera al mensaje `Started PricesApplication` y deja la consola abierta. La aplicación escucha en
`http://localhost:8080` con una base de datos H2 en memoria ya cargada con los datos del enunciado.
Para pararla, `Ctrl+C`.

También se puede ejecutar el jar:

```bash
./mvnw clean package
java -jar target/prices-1.0.0.jar
```

### 5. Hacer una consulta

Desde el navegador:

```
http://localhost:8080/api/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1
```

Desde la terminal (en PowerShell usa `curl.exe`, porque `curl` es un alias de otro comando):

```bash
curl "http://localhost:8080/api/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1"
```

Respuesta:

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 2,
  "startDate": "2020-06-14T15:00:00",
  "endDate": "2020-06-14T18:30:00",
  "price": 25.45,
  "currency": "EUR"
}
```

Los 5 casos del enunciado (producto 35455, cadena 1):

| Test | `applicationDate`     | Tarifa | Precio |
|------|-----------------------|--------|--------|
| 1    | `2020-06-14T10:00:00` | 1      | 35.50  |
| 2    | `2020-06-14T16:00:00` | 2      | 25.45  |
| 3    | `2020-06-14T21:00:00` | 1      | 35.50  |
| 4    | `2020-06-15T10:00:00` | 3      | 30.50  |
| 5    | `2020-06-16T21:00:00` | 4      | 38.95  |

También se puede probar con Swagger UI, en
[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) (**Try it out**).

### 6. Si algo falla

| Síntoma | Solución |
|---|---|
| `JAVA_HOME is not defined` o no encuentra Java | Instala un JDK 17+ y define `JAVA_HOME` (PowerShell: `$env:JAVA_HOME = "C:\ruta\al\jdk"`) |
| `Permission denied` al ejecutar `./mvnw` | `chmod +x mvnw`, o ejecútalo con `sh mvnw ...` |
| `Port 8080 was already in use` | Cierra el proceso que lo usa, o arranca en otro puerto: `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081` |
| No descarga dependencias | Comprueba la conexión a internet o el proxy de la red |
| `mvn` no se reconoce como comando | Usa `mvnw` (o `.\mvnw.cmd` en Windows), no `mvn` |

## API

```
GET /api/prices?applicationDate={fecha-hora}&productId={id}&brandId={id}
```

| Parámetro         | Tipo                | Descripción                                           |
|-------------------|---------------------|-------------------------------------------------------|
| `applicationDate` | fecha-hora ISO-8601 | Fecha de aplicación, sin zona (`2020-06-14T10:00:00`) |
| `productId`       | entero              | Identificador del producto                            |
| `brandId`         | entero              | Identificador de la cadena (1 = ZARA)                 |

| Código | Cuándo                                                                   |
|--------|--------------------------------------------------------------------------|
| `200`  | Precio encontrado                                                        |
| `404`  | No hay ninguna tarifa aplicable (también producto o cadena inexistentes) |
| `400`  | Parámetro ausente o con formato incorrecto                               |

Los errores se devuelven como `ProblemDetail` (RFC 7807). El contrato completo está en
`src/main/resources/static/openapi/prices-api.yaml`.

## Tests

```bash
./mvnw clean verify
```

| Tipo       | Clase                           | Qué valida                                                                   |
|------------|---------------------------------|------------------------------------------------------------------------------|
| Unitario   | `GetApplicablePriceServiceTest` | Caso de uso con el puerto de salida simulado: devuelve el precio o lanza `PriceNotFoundException` |
| Unitario   | `PriceRepositoryAdapterTest`    | La consulta contra H2: mapeo, prioridad, fronteras de fechas, marca o producto inexistente y foreign key hacia `BRAND` |
| Unitario   | `PriceControllerTest`           | Capa web (`@WebMvcTest`): mapeo de la respuesta, 404 y 400                   |
| End-to-end | `PriceApiE2ETest`               | Aplicación real + HTTP (RestAssured): los 5 casos del enunciado, 404 por producto o marca inexistente y 400 |
| End-to-end | `PriceBoundaryE2ETest`          | Fronteras de los rangos de fechas por HTTP, con datos propios  |

## Arquitectura

Arquitectura hexagonal (puertos y adaptadores):

```
domain/            modelo (Price), excepción y puertos in/out. Sin dependencias de frameworks
application/       caso de uso (GetApplicablePriceService)
infrastructure/
  rest/            adaptador de entrada: controller, mapper y manejo de errores
  persistence/     adaptador de salida: entidades JPA y repositorio Spring Data
```

### Modelo de datos

- `BRAND(ID, NAME)`: cadenas del grupo. Solo contiene ZARA, la única que menciona el enunciado.
- `PRICES`: tarifas, con `BRAND_ID` como **foreign key** hacia `BRAND`.
- En JPA, `PriceEntity` modela la relación con `@ManyToOne` hacia `BrandEntity` (carga perezosa; la
  consulta usa un `@EntityGraph` para traer la cadena en la misma query).

## Base de datos y portabilidad

### Preparado para cambiar de base de datos

La aplicación no depende de una base de datos concreta, sino de una interfaz. Es la misma idea que el patrón
DAO, con la interfaz en el dominio:

| Pieza | Papel | Equivale a |
|---|---|---|
| `PriceRepositoryPort` (dominio) | Lo que la aplicación necesita del almacén de tarifas. No sabe de SQL ni de JPA | La interfaz del DAO, propiedad del dominio |
| `PriceRepositoryAdapter` (infraestructura) | Implementa el puerto y convierte entidades en modelo de dominio | El repositorio o DAO |
| `PriceJpaRepository` (infraestructura) | Consulta de Spring Data JPA; Spring genera la implementación | La implementación del DAO |

Qué habría que tocar en cada caso:

| Cambio | Se toca | No se toca |
|---|---|---|
| Otro motor relacional (por ejemplo Oracle o MySQL) | Dependencia del driver, `spring.datasource.*` y los scripts SQL, que pueden necesitar ajustes según el motor (por ejemplo la columna autogenerada) | Dominio, aplicación, adaptador, entidades y consulta: es una consulta derivada de Spring Data, sin SQL nativo ni funciones propias de H2 |
| Otra tecnología (JDBC, MongoDB, un servicio remoto) | Un nuevo adaptador que implemente `PriceRepositoryPort` | Dominio, aplicación y capa REST |

## Autenticación y autorización (no aplicada en esta prueba)

El enunciado define un endpoint de consulta de solo lectura y no pide autenticación, por lo que **no se ha
incluido Spring Security**. Añadirlo por defecto bloquearía todos los endpoints (incluidos Swagger UI y
los tests) hasta configurarlo. 

## Integración continua

`.github/workflows/ci.yml` ejecuta `./mvnw clean verify` con JDK 17 en cada push a `main` y `desarrollo` y en
cada Pull Request hacia `main`. Si falla, sube los informes de Surefire como artefacto. El resultado se ve en la
pestaña **Actions** del repositorio y en la insignia de arriba.

## Decisiones

- **API first**: el contrato OpenAPI es la fuente de verdad. `openapi-generator-maven-plugin` genera en
  el build la interfaz `PricesApi` y el DTO `PriceResponse`, y `PriceController` implementa esa interfaz.
  Swagger UI (springdoc) muestra ese mismo fichero.
- **La selección de la tarifa se resuelve en la consulta**: filtra por cadena, producto y rango de fechas,
  ordena por prioridad descendente y devuelve solo la primera fila. No se traen a memoria las tarifas que no
  aplican.
- **Rangos inclusivos**: una tarifa aplica desde su `startDate` hasta su `endDate`, ambos incluidos.
- **404 para "no hay precio"**: es un recurso inexistente, no una petición mal formada. Se usa 400
  solo para parámetros ausentes o inválidos.
- **Tipos**: `LocalDateTime` para fechas y `BigDecimal` para el precio (nunca `double`).
- **Dependencias mínimas**: web, data-jpa, validation (anotaciones del contrato), H2, springdoc y, en
  test, RestAssured.
- **Empate de prioridad**: el enunciado no lo contempla. La consulta solo ordena por prioridad, así que si
    dos tarifas con la misma prioridad cubren la fecha el resultado no está determinado.
- **Versión de Spring Boot**: 3.3.5 con Java 17. La línea 3.x ya no recibe correcciones gratuitas; la
    evolución natural es migrar a Spring Boot 4.x.

## Discutido y no implementado

| Idea | Por qué no |
|---|---|
| Caché de tarifas (local, distribuida, invalidación) | Se evaluó cachear la lista de tarifas por (marca, producto). No hay datos de carga que lo justifiquen, la BD es H2 en memoria y añadiría riesgo de precios desfasados (correcciones urgentes, varias instancias) y de resultados vacíos cacheados. Además obligaba a traer todas las tarifas y filtrar en el dominio |
| Spring Security (OAuth2 / JWT) | El enunciado no pide autenticación. Comentado arriba |
| Otro motor de base de datos, migraciones (Flyway) y Testcontainers | H2 en memoria cumple el enunciado. El código está preparado para el cambio (ver *Preparado para cambiar de base de datos*) |
| Enfoque DDD completo (value objects, agregado `ProductPricing`) | Más clases y mapeos de los que justifica el tamaño de la prueba. Sería la evolución natural: `ApplicationPeriod`, `Money`, `BrandId`, `ProductId`, y un agregado con el método `priceAt(fecha)` |
| Karate o Cucumber para los E2E | RestAssured ya cubre el criterio con código Java y sin lenguaje adicional. Cucumber tendría sentido si negocio debiera leer los escenarios |
| JaCoCo con umbral de cobertura, MapStruct | No aportan a lo pedido; JaCoCo sería lo más defendible y habría que excluir el código generado |
| Objeto `Brand` en el dominio o nombre de la cadena en la respuesta | El enunciado solo pide el identificador. La relación se modela en persistencia; `BrandEntity.name` se mapea pero no se expone |
| Lombok | Con *records* y mapeos de un método no compensa la dependencia |
| `@Service` fuera de la capa de aplicación (registrar el bean a mano) | Sería más puro, pero añade una clase de configuración. El dominio sí está libre de frameworks |
| Logs de depuración, Actuator, Dockerfile | Fuera del alcance de lo pedido |
| Documentar la API por anotaciones (code first) | Se eligió API first: el contrato se escribe primero y el código se genera |
