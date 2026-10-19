package com.inditex.prices.infrastructure.rest;

import com.inditex.prices.domain.exception.PriceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce los errores a respuestas RFC 7807 ({@link ProblemDetail}).
 *
 * <p>Los parámetros ausentes o con formato incorrecto (400) los resuelve
 * {@link ResponseEntityExceptionHandler}; aquí solo se añade el error de negocio.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Sin tarifa aplicable: 404.
     */
    @ExceptionHandler(PriceNotFoundException.class)
    ProblemDetail handlePriceNotFound(PriceNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Price not found");
        return problem;
    }
}
