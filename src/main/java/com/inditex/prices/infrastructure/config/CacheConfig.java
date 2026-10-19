package com.inditex.prices.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Activa la caché de Spring. Los parámetros (tamaño y caducidad) están en {@code application.yml}.
 * Se mantiene en una clase aparte para que los tests de capa web y de persistencia no la carguen.
 */
@Configuration
@EnableCaching
class CacheConfig {
}
