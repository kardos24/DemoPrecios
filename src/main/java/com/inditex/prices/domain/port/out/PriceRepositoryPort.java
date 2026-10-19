package com.inditex.prices.domain.port.out;

import com.inditex.prices.domain.model.Price;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Puerto de salida: acceso al catálogo de tarifas.
 */
public interface PriceRepositoryPort {

    /**
     * Devuelve la tarifa vigente en la fecha indicada. Los extremos del rango son inclusivos.
     * Si varias tarifas están vigentes se devuelve la de mayor prioridad.
     *
     * @param applicationDate fecha y hora de aplicación
     * @param productId       identificador del producto
     * @param brandId         identificador de la cadena
     * @return la tarifa aplicable, o vacío si ninguna está vigente
     */
    Optional<Price> findApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId);
}
