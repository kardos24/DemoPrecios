package com.inditex.prices.application;

import com.inditex.prices.domain.exception.PriceNotFoundException;
import com.inditex.prices.domain.model.Price;
import com.inditex.prices.domain.port.in.GetApplicablePriceUseCase;
import com.inditex.prices.domain.port.out.PriceRepositoryPort;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: obtiene la tarifa aplicable a un producto de una cadena en una fecha.
 * La selección entre tarifas solapadas (prioridad y desempate) la resuelve el puerto de salida.
 */
@Service
public class GetApplicablePriceService implements GetApplicablePriceUseCase {

    private final PriceRepositoryPort priceRepository;

    public GetApplicablePriceService(PriceRepositoryPort priceRepository) {
        this.priceRepository = priceRepository;
    }

    @Override
    public Price getApplicablePrice(LocalDateTime applicationDate, Long productId, Long brandId) {
        return priceRepository.findApplicablePrice(applicationDate, productId, brandId)
                .orElseThrow(() -> new PriceNotFoundException(productId, brandId, applicationDate));
    }
}
