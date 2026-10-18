package com.inditex.prices.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface PriceJpaRepository extends JpaRepository<PriceEntity, Long> {

    Optional<PriceEntity>
            findFirstByBrandIdAndProductIdAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDesc(
                    Long brandId, Long productId, LocalDateTime startDate, LocalDateTime endDate);
}
