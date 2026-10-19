package com.inditex.prices.infrastructure.rest;

import com.inditex.prices.domain.model.Price;
import com.inditex.prices.infrastructure.rest.model.PriceResponse;
import org.springframework.stereotype.Component;

/**
 * Convierte el modelo de dominio en el DTO de respuesta generado desde el contrato.
 */
@Component
public class PriceRestMapper {

    PriceResponse toResponse(Price price) {
        return new PriceResponse()
                .productId(price.productId())
                .brandId(price.brandId())
                .priceList(price.priceList())
                .startDate(price.startDate())
                .endDate(price.endDate())
                .price(price.price())
                .currency(price.currency());
    }
}
