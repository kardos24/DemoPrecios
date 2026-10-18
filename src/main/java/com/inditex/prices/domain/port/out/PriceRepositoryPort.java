package com.inditex.prices.domain.port.out;

import com.inditex.prices.domain.model.Price;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PriceRepositoryPort {

    /**
     * Devuelve el precio vigente en la fecha indicada; si varios coinciden, el de mayor prioridad.
     */
    Optional<Price> findApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId);
}
