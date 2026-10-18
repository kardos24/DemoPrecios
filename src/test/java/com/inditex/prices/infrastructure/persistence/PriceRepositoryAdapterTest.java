package com.inditex.prices.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(PriceRepositoryAdapter.class)
class PriceRepositoryAdapterTest {

    private static final long PRODUCT_ID = 35455L;
    private static final long BRAND_ID = 1L;

    @Autowired
    private PriceRepositoryAdapter adapter;

    @Test
    void picksTheHighestPriorityPriceWhenSeveralOverlap() {
        var price = adapter.findApplicablePrice(LocalDateTime.of(2020, 6, 14, 16, 0), PRODUCT_ID, BRAND_ID);

        assertThat(price).hasValueSatisfying(p -> {
            assertThat(p.priceList()).isEqualTo(2);
            assertThat(p.price()).isEqualByComparingTo("25.45");
            assertThat(p.currency()).isEqualTo("EUR");
        });
    }

    @Test
    void returnsEmptyWhenNoPriceCoversTheDate() {
        var price = adapter.findApplicablePrice(LocalDateTime.of(2019, 12, 31, 23, 59), PRODUCT_ID, BRAND_ID);

        assertThat(price).isEmpty();
    }

    @Test
    void returnsEmptyForAnUnknownBrandOrProduct() {
        var date = LocalDateTime.of(2020, 6, 14, 10, 0);

        assertThat(adapter.findApplicablePrice(date, PRODUCT_ID, 2L)).isEmpty();
        assertThat(adapter.findApplicablePrice(date, 99999L, BRAND_ID)).isEmpty();
    }
}
