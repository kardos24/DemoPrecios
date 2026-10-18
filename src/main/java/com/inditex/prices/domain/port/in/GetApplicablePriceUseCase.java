package com.inditex.prices.domain.port.in;

import com.inditex.prices.domain.model.Price;
import java.time.LocalDateTime;

public interface GetApplicablePriceUseCase {

    /**
     * @throws com.inditex.prices.domain.exception.PriceNotFoundException si no hay precio aplicable
     */
    Price getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId);
}
