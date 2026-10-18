package com.inditex.prices.infrastructure.persistence;

import com.inditex.prices.domain.model.Price;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PRICES")
class PriceEntity {

    @Id
    private Long id;
    private Long brandId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer priceList;
    private Long productId;
    private Integer priority;
    private BigDecimal price;
    @Column(name = "CURR")
    private String currency;

    Price toDomain() {
        return new Price(brandId, productId, priceList, startDate, endDate, price, currency);
    }
}
