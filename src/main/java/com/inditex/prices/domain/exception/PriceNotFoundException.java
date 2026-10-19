package com.inditex.prices.domain.exception;

import java.time.LocalDateTime;

/**
 * Se lanza cuando ninguna tarifa es aplicable al producto, cadena y fecha consultados.
 */
public class PriceNotFoundException extends RuntimeException {

    /**
     * @param productId       producto consultado
     * @param brandId         cadena consultada
     * @param applicationDate fecha de aplicación consultada
     */
    public PriceNotFoundException(Long productId, Long brandId, LocalDateTime applicationDate) {
        super("No applicable price for product %d and brand %d at %s"
                .formatted(productId, brandId, applicationDate));
    }
}
