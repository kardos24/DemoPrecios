package com.inditex.prices.domain.port.in;

import com.inditex.prices.domain.model.Price;
import java.time.LocalDateTime;

/**
 * Puerto de entrada: consulta del precio aplicable a un producto.
 */
public interface GetApplicablePriceUseCase {

    /**
     * Obtiene la tarifa que aplica a un producto de una cadena en una fecha.
     *
     * @param applicationDate fecha y hora de aplicación
     * @param productId       identificador del producto
     * @param brandId         identificador de la cadena
     * @return la tarifa aplicable
     * @throws com.inditex.prices.domain.exception.PriceNotFoundException si no hay ninguna vigente
     */
    Price getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId);
}
