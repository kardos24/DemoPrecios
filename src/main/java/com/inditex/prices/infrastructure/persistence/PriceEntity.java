package com.inditex.prices.infrastructure.persistence;

import com.inditex.prices.domain.model.Price;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fila de la tabla {@code PRICES}. Solo existe en la capa de persistencia: el dominio trabaja
 * con {@link Price}. La prioridad se mapea porque la consulta ordena por ella, pero no forma
 * parte del modelo de dominio.
 */
@Entity
@Table(name = "PRICES")
class PriceEntity {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BRAND_ID", nullable = false)
    private BrandEntity brand;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer priceList;
    private Long productId;
    private Integer priority;
    private BigDecimal price;

    @Column(name = "CURR")
    private String currency;

    /**
     * Convierte la entidad en el modelo de dominio. La cadena debe estar cargada
     * (ver {@link PriceJpaRepository}).
     */
    Price toDomain() {
        return new Price(brand.getId(), productId, priceList, startDate, endDate, price, currency);
    }
}
