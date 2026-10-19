package com.inditex.prices.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tarifa aplicable a un producto de una cadena durante un rango de fechas.
 *
 * @param brandId   identificador de la cadena del grupo (1 = ZARA)
 * @param productId identificador del producto
 * @param priceList identificador de la tarifa
 * @param startDate inicio del rango de aplicación (inclusive)
 * @param endDate   fin del rango de aplicación (inclusive)
 * @param price     precio final de venta
 * @param currency  código ISO de la moneda
 */
public record Price(
        Long brandId,
        Long productId,
        Integer priceList,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal price,
        String currency) {
}
