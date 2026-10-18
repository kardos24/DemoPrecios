# Prices service

Servicio REST (Spring Boot) que devuelve el precio aplicable a un producto de una cadena en una fecha
dada. Si varias tarifas coinciden en esa fecha, se aplica la de mayor prioridad.

## Requisitos

- Java 17 o superior
- Maven 3.9+

## Ejecutar

```bash
mvn spring-boot:run
```

La aplicación arranca en `http://localhost:8080` con una base de datos H2 en memoria que se inicializa
con los datos del enunciado (`schema.sql` y `data.sql`).

## Probar el endpoint

```bash
curl "http://localhost:8080/api/prices?applicationDate=2020-06-14T16:00:00&productId=35455&brandId=1"
```

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

| Parámetro         | Tipo                 | Descripción                                   |
|-------------------|----------------------|-----------------------------------------------|
| `applicationDate` | fecha-hora ISO-8601  | Fecha de aplicación, sin zona (`2020-06-14T10:00:00`) |
| `productId`       | entero               | Identificador del producto                    |
| `brandId`         | entero               | Identificador de la cadena (1 = ZARA)         |

Códigos de respuesta:

- `200`: precio encontrado.
- `404`: no hay ningún precio aplicable para esos criterios.
- `400`: parámetro ausente o con formato incorrecto.

Los errores se devuelven como `ProblemDetail` (RFC 7807).

## Tests

```bash
mvn test
```

| Tipo       | Clase                           | Qué valida                                                         |
|------------|---------------------------------|--------------------------------------------------------------------|
| Unitario   | `GetApplicablePriceServiceTest` | Caso de uso con el puerto de salida mockeado                       |
| Unitario   | `PriceRepositoryAdapterTest`    | Consulta JPA: prioridad, fechas fuera de rango, marca/producto inexistentes |
| Unitario   | `PriceControllerTest`           | Capa web (`@WebMvcTest`): mapeo de respuesta, 404 y 400            |
| End-to-end | `PriceApiE2ETest`               | Aplicación real + HTTP con RestAssured: los 5 casos del enunciado, 404 y 400 |

Los 5 casos del enunciado:

| Test | Petición            | Tarifa | Precio |
|------|---------------------|--------|--------|
| 1    | día 14 a las 10:00  | 1      | 35.50  |
| 2    | día 14 a las 16:00  | 2      | 25.45  |
| 3    | día 14 a las 21:00  | 1      | 35.50  |
| 4    | día 15 a las 10:00  | 3      | 30.50  |
| 5    | día 16 a las 21:00  | 4      | 38.95  |

## Arquitectura

Arquitectura hexagonal (puertos y adaptadores):

```
domain/            modelo (Price), excepción y puertos (in / out). Sin dependencias de frameworks
application/       caso de uso (GetApplicablePriceService)
infrastructure/
  rest/            adaptador de entrada: controller, mapper y manejo de errores
  persistence/     adaptador de salida: entidad JPA, repositorio Spring Data y adaptador
```

## Decisiones

- **API first**: el contrato está en `src/main/resources/openapi/prices-api.yaml`.
  `openapi-generator-maven-plugin` genera durante el build la interfaz `PricesApi` y el DTO `PriceResponse`
  (en `target/generated-sources`); `PriceController` implementa esa interfaz.
- **La prioridad se resuelve en base de datos**: la consulta filtra por cadena, producto y rango de fechas,
  ordena por `PRIORITY DESC` y devuelve solo la primera fila.
- **Tipos**: `LocalDateTime` para las fechas, `BigDecimal` para el precio.
- **Esquema**: lo define `schema.sql` (`ddl-auto: none`), con un índice sobre los campos de búsqueda.
- **Dependencias mínimas**: web, data-jpa, validation (anotaciones del contrato), H2 y, en test, RestAssured.
