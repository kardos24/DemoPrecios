package com.inditex.prices.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Prueba la consulta contra H2. La regla de selección (extremos inclusivos y prioridad)
 * vive en la consulta, así que se verifica con datos propios de la prueba, no con los de
 * {@code data.sql}. Cada test se ejecuta en una transacción que se deshace al terminar.
 *
 * <p>Fixture, el día 2030-01-01, producto 90001: tarifa 20 (prioridad 0, todoo el día) y
 * tarifa 21 (prioridad 1, de 10:00:00 a 12:00:00).
 */
@DataJpaTest
@Import(PriceRepositoryAdapter.class)
class PriceRepositoryAdapterTest {

    private static final long BRAND_ID = 1L;
    private static final long SEED_PRODUCT_ID = 35455L;
    private static final long PRIORITY_PRODUCT_ID = 90001L;

    @Autowired
    private PriceRepositoryAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void createFixture() {
        insertTariff( 20, "2030-01-01 00:00:00", "2030-01-01 23:59:59", 0);
        insertTariff( 21, "2030-01-01 10:00:00", "2030-01-01 12:00:00", 1);
    }

    @Test
    void mapsTheFieldsOfTheTariff() {
        var price = adapter.findApplicablePrice(LocalDateTime.of(2020, 6, 14, 16, 0), SEED_PRODUCT_ID, BRAND_ID);

        assertThat(price).hasValueSatisfying(p -> {
            assertThat(p.brandId()).isEqualTo(BRAND_ID);
            assertThat(p.productId()).isEqualTo(SEED_PRODUCT_ID);
            assertThat(p.priceList()).isEqualTo(2);
            assertThat(p.startDate()).hasToString("2020-06-14T15:00");
            assertThat(p.endDate()).hasToString("2020-06-14T18:30");
            assertThat(p.price()).isEqualByComparingTo("25.45");
            assertThat(p.currency()).isEqualTo("EUR");
        });
    }

    @Test
    void picksTheHighestPriorityWhenSeveralTariffsOverlap() {
        var price = adapter.findApplicablePrice(LocalDateTime.of(2030, 1, 1, 11, 0), PRIORITY_PRODUCT_ID, BRAND_ID);

        assertThat(price).hasValueSatisfying(p -> assertThat(p.priceList()).isEqualTo(21));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            Un segundo antes del inicio de la tarifa 21 | 2030-01-01T09:59:59 | 20
            Inicio exacto de la tarifa 21               | 2030-01-01T10:00:00 | 21
            Fin exacto de la tarifa 21                  | 2030-01-01T12:00:00 | 21
            Un segundo después del fin de la tarifa 21  | 2030-01-01T12:00:01 | 20
            Inicio exacto de la tarifa 20               | 2030-01-01T00:00:00 | 20
            Fin exacto de la tarifa 20                  | 2030-01-01T23:59:59 | 20
            """)
    void appliesTheRangeExtremesInclusively(String description, LocalDateTime date, int priceList) {
        var price = adapter.findApplicablePrice(date, PRIORITY_PRODUCT_ID, BRAND_ID);

        assertThat(price).hasValueSatisfying(p -> assertThat(p.priceList()).isEqualTo(priceList));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            Un segundo antes de todas las tarifas   | 2029-12-31T23:59:59
            Un segundo después de todas las tarifas | 2030-01-02T00:00:00
            """)
    void returnsEmptyJustOutsideTheWholeRange(String description, LocalDateTime date) {
        assertThat(adapter.findApplicablePrice(date, PRIORITY_PRODUCT_ID, BRAND_ID)).isEmpty();
    }

    @Test
    void returnsEmptyForAnUnknownBrandOrProduct() {
        var date = LocalDateTime.of(2020, 6, 14, 10, 0);

        assertThat(adapter.findApplicablePrice(date, SEED_PRODUCT_ID, 2L)).isEmpty();
        assertThat(adapter.findApplicablePrice(date, 99999L, BRAND_ID)).isEmpty();
    }

    @Test
    void rejectsATariffOfAnUnknownBrand() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO PRICES (BRAND_ID, START_DATE, END_DATE, PRICE_LIST, PRODUCT_ID, PRIORITY, PRICE, CURR)
                VALUES (99, '2030-01-01 00:00:00', '2030-12-31 23:59:59', 9, 90003, 0, 10.00, 'EUR')
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void insertTariff(int priceList, String start, String end, int priority) {
        jdbcTemplate.update("""
                INSERT INTO PRICES (BRAND_ID, START_DATE, END_DATE, PRICE_LIST, PRODUCT_ID, PRIORITY, PRICE, CURR)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'EUR')
                """,
                BRAND_ID, Timestamp.valueOf(start), Timestamp.valueOf(end), priceList, PRIORITY_PRODUCT_ID,
                priority, BigDecimal.TEN);
    }
}
