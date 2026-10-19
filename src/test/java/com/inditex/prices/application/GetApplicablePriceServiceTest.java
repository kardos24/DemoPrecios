package com.inditex.prices.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.inditex.prices.domain.exception.PriceNotFoundException;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.port.out.PriceRepositoryPort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetApplicablePriceServiceTest {

    private static final LocalDateTime DATE = LocalDateTime.of(2020, 6, 14, 10, 0);
    private static final long PRODUCT_ID = 35455L;
    private static final long BRAND_ID = 1L;

    @Mock
    private PriceRepositoryPort priceRepository;

    @InjectMocks
    private GetApplicablePriceService service;

    @Test
    void returnsThePriceFoundByTheRepository() {
        var expected = new Price(BRAND_ID, PRODUCT_ID, 1, DATE.minusHours(10), DATE.plusMonths(6),
                new BigDecimal("35.50"), "EUR");
        when(priceRepository.findApplicablePrice(DATE, PRODUCT_ID, BRAND_ID)).thenReturn(Optional.of(expected));

        assertThat(service.getApplicablePrice(DATE, PRODUCT_ID, BRAND_ID)).isEqualTo(expected);
    }

    @Test
    void throwsPriceNotFoundWhenNoPriceApplies() {
        when(priceRepository.findApplicablePrice(DATE, PRODUCT_ID, BRAND_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getApplicablePrice(DATE, PRODUCT_ID, BRAND_ID))
                .isInstanceOf(PriceNotFoundException.class)
                .hasMessageContaining("35455");
    }
}
