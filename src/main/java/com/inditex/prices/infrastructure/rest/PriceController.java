package com.inditex.prices.infrastructure.rest;

import com.inditex.prices.domain.port.in.GetApplicablePriceUseCase;
import com.inditex.prices.infrastructure.rest.api.PricesApi;
import com.inditex.prices.infrastructure.rest.model.PriceResponse;
import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PriceController implements PricesApi {

    private final GetApplicablePriceUseCase getApplicablePrice;
    private final PriceRestMapper mapper;

    public PriceController(GetApplicablePriceUseCase getApplicablePrice, PriceRestMapper mapper) {
        this.getApplicablePrice = getApplicablePrice;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<PriceResponse> getApplicablePrice(
            LocalDateTime applicationDate, Long productId, Long brandId) {
        var price = getApplicablePrice.getApplicablePrice(applicationDate, productId, brandId);
        return ResponseEntity.ok(mapper.toResponse(price));
    }
}
