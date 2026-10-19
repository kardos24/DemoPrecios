package com.inditex.prices.infrastructure.persistence;

import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.port.out.PriceRepositoryPort;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de salida hacia la base de datos. La selección de la tarifa aplicable (vigencia,
 * prioridad y desempate) se resuelve en la propia consulta, que devuelve una sola fila.
 */
@Repository
public class PriceRepositoryAdapter implements PriceRepositoryPort {

    private final PriceJpaRepository repository;

    PriceRepositoryAdapter(PriceJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Price> findApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        return repository
                .findFirstByBrand_IdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                        brandId, productId, applicationDate, applicationDate)
                .map(PriceEntity::toDomain);
    }
}
