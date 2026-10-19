package com.inditex.prices.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

/**
 * Repositorio Spring Data. Spring genera la implementación en tiempo de ejecución a partir
 * del nombre del método. Solo expone la consulta que necesita la aplicación.
 */
interface PriceJpaRepository extends Repository<PriceEntity, Long> {

    /**
     * Tarifa vigente de un producto en una cadena: {@code startDate <= fecha <= endDate}, ordenada por
     * prioridad descendente; devuelve solo la primera. Se pasa la misma fecha en {@code startDate}
     * y {@code endDate}.
     *
     * <p>El grafo carga la cadena en la misma consulta para poder leer su identificador fuera de la
     * sesión de Hibernate.
     */
    @EntityGraph(attributePaths = "brand")
    Optional<PriceEntity>
    findFirstByBrand_IdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
            Long brandId, Long productId, LocalDateTime startDate, LocalDateTime endDate);
}
