package com.inditex.prices.infrastructure.rest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.inditex.prices.domain.exception.PriceNotFoundException;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.port.in.GetApplicablePriceUseCase;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PriceController.class)
@Import(PriceRestMapper.class)
class PriceControllerTest {

    private static final String PATH = "/api/prices";
    private static final LocalDateTime DATE = LocalDateTime.of(2020, 6, 14, 10, 0);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetApplicablePriceUseCase useCase;

    @Test
    void returnsThePriceWith200() throws Exception {
        when(useCase.getApplicablePrice(DATE, 35455L, 1L)).thenReturn(new Price(
                1L, 35455L, 1, LocalDateTime.of(2020, 6, 14, 0, 0), LocalDateTime.of(2020, 12, 31, 23, 59, 59),
                new BigDecimal("35.50"), "EUR"));

        mockMvc.perform(get(PATH)
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(35455))
                .andExpect(jsonPath("$.brandId").value(1))
                .andExpect(jsonPath("$.priceList").value(1))
                .andExpect(jsonPath("$.startDate").value("2020-06-14T00:00:00"))
                .andExpect(jsonPath("$.endDate").value("2020-12-31T23:59:59"))
                .andExpect(jsonPath("$.price").value(35.50))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void returns404WhenThereIsNoApplicablePrice() throws Exception {
        when(useCase.getApplicablePrice(DATE, 35455L, 1L))
                .thenThrow(new PriceNotFoundException(35455L, 1L, DATE));

        mockMvc.perform(get(PATH)
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Price not found"));
    }

    @Test
    void returns400WhenAParameterIsMissing() throws Exception {
        mockMvc.perform(get(PATH)
                        .param("applicationDate", "2020-06-14T10:00:00")
                        .param("productId", "35455"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400WhenAParameterHasAnInvalidFormat() throws Exception {
        mockMvc.perform(get(PATH)
                        .param("applicationDate", "not-a-date")
                        .param("productId", "35455")
                        .param("brandId", "1"))
                .andExpect(status().isBadRequest());
    }
}
