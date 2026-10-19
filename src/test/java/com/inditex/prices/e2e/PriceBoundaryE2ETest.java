package com.inditex.prices.e2e;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.response.ValidatableResponse;
import java.math.BigDecimal;
import java.sql.Timestamp;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Pruebas de frontera de los rangos de fechas. Verifican la <em>regla</em> (extremos inclusivos y
 * resolución por prioridad), no los datos: la clase crea su propio fixture, con un producto que no
 * existe en {@code data.sql}, de modo que no se rompen si cambian los datos iniciales.
 *
 * <p>Una frontera tiene cuatro comportamientos distintos que probar: un segundo antes del inicio,
 * el inicio exacto, el fin exacto y un segundo después del fin. No hace falta repetirlos por cada
 * tarifa del catálogo, porque todas pasan por el mismo código.
 *
 * <p>Fixture (producto 90001, cadena 1, día 2030-01-01):
 * <ul>
 *   <li>tarifa 20, prioridad 0: todo el día (00:00:00 a 23:59:59)</li>
 *   <li>tarifa 21, prioridad 1: de 10:00:00 a 12:00:00</li>
 * </ul>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PriceBoundaryE2ETest {

    private static final String PATH = "/api/prices";
    private static final long PRODUCT_ID = 90001L;
    private static final long BRAND_ID = 1L;

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void createFixture() {
        insertTariff(20, "2030-01-01 00:00:00", "2030-01-01 23:59:59", 0);
        insertTariff(21, "2030-01-01 10:00:00", "2030-01-01 12:00:00", 1);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            Un segundo antes del inicio de la tarifa 21 | 2030-01-01T09:59:59 | 20
            Inicio exacto de la tarifa 21               | 2030-01-01T10:00:00 | 21
            Fin exacto de la tarifa 21                  | 2030-01-01T12:00:00 | 21
            Un segundo después del fin de la tarifa 21  | 2030-01-01T12:00:01 | 20
            """)
    void appliesTheRangeExtremesInclusively(String description, String applicationDate, int priceList) {
        query(applicationDate)
                .statusCode(200)
                .body("priceList", equalTo(priceList));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            Un segundo antes de todas las tarifas   | 2029-12-31T23:59:59
            Un segundo después de todas las tarifas | 2030-01-02T00:00:00
            """)
    void returns404JustOutsideTheWholeCatalogRange(String description, String applicationDate) {
        query(applicationDate)
                .statusCode(404)
                .body("title", equalTo("Price not found"));
    }

    private ValidatableResponse query(String applicationDate) {
        return given().port(port).accept("application/json")
                .queryParam("applicationDate", applicationDate)
                .queryParam("productId", PRODUCT_ID)
                .queryParam("brandId", BRAND_ID)
                .when().get(PATH)
                .then();
    }

    private void insertTariff(int priceList, String start, String end, int priority) {
        jdbcTemplate.update("""
                INSERT INTO PRICES (BRAND_ID, START_DATE, END_DATE, PRICE_LIST, PRODUCT_ID, PRIORITY, PRICE, CURR)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'EUR')
                """,
                BRAND_ID, Timestamp.valueOf(start), Timestamp.valueOf(end), priceList, PRODUCT_ID,
                priority, BigDecimal.TEN);
    }
}
