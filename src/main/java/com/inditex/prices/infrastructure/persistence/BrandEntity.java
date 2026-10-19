package com.inditex.prices.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Cadena del grupo (tabla {@code BRAND}). Es el destino de la foreign key de {@link PriceEntity}.
 * El nombre se mapea por completitud, pero la API no lo expone.
 */
@Entity
@Table(name = "BRAND")
class BrandEntity {

    @Id
    private Long id;
    private String name;

    Long getId() {
        return id;
    }
}
