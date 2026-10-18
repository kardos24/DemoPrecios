package com.inditex.prices.e2e;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.inditex.prices.infrastructure.rest.model.PriceResponse;
import io.restassured.specification.RequestSpecification;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Pruebas end-to-end: arrancan la aplicación real (con H2 y los datos del enunciado)
 * y la consultan por HTTP con RestAssured.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PriceApiE2ETest {

    private static final String PATH = "/api/prices";
    private static final long PRODUCT_ID = 35455L;
    private static final long BRAND_ID = 1L;

    @LocalServerPort
    private int port;

    private RequestSpecification request() {
        return given().port(port).accept("application/json");
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            Test 1: 10:00 del día 14 | 2020-06-14T10:00:00 | 1 | 2020-06-14T00:00:00 | 2020-12-31T23:59:59 | 35.50
            Test 2: 16:00 del día 14 | 2020-06-14T16:00:00 | 2 | 2020-06-14T15:00:00 | 2020-06-14T18:30:00 | 25.45
            Test 3: 21:00 del día 14 | 2020-06-14T21:00:00 | 1 | 2020-06-14T00:00:00 | 2020-12-31T23:59:59 | 35.50
            Test 4: 10:00 del día 15 | 2020-06-15T10:00:00 | 3 | 2020-06-15T00:00:00 | 2020-06-15T11:00:00 | 30.50
            Test 5: 21:00 del día 16 | 2020-06-16T21:00:00 | 4 | 2020-06-15T16:00:00 | 2020-12-31T23:59:59 | 38.95
            """)
    void returnsTheApplicablePrice(String description, String applicationDate, int priceList,
                                   LocalDateTime startDate, LocalDateTime endDate, BigDecimal price) {
        var response = request()
                .queryParam("applicationDate", applicationDate)
                .queryParam("productId", PRODUCT_ID)
                .queryParam("brandId", BRAND_ID)
                .when().get(PATH)
                .then().statusCode(200)
                .extract().as(PriceResponse.class);

        assertThat(response.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getBrandId()).isEqualTo(BRAND_ID);
        assertThat(response.getPriceList()).isEqualTo(priceList);
        assertThat(response.getStartDate()).isEqualTo(startDate);
        assertThat(response.getEndDate()).isEqualTo(endDate);
        assertThat(response.getPrice()).isEqualByComparingTo(price);
        assertThat(response.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void returns404WhenNoPriceIsApplicable() {
        request()
                .queryParam("applicationDate", "2019-01-01T10:00:00")
                .queryParam("productId", PRODUCT_ID)
                .queryParam("brandId", BRAND_ID)
                .when().get(PATH)
                .then().statusCode(404)
                .body("title", equalTo("Price not found"));
    }

    @Test
    void returns400WhenAParameterIsMissing() {
        request()
                .queryParam("applicationDate", "2020-06-14T10:00:00")
                .queryParam("productId", PRODUCT_ID)
                .when().get(PATH)
                .then().statusCode(400);
    }

    @Test
    void returns400WhenTheDateIsInvalid() {
        request()
                .queryParam("applicationDate", "14/06/2020")
                .queryParam("productId", PRODUCT_ID)
                .queryParam("brandId", BRAND_ID)
                .when().get(PATH)
                .then().statusCode(400);
    }
}
