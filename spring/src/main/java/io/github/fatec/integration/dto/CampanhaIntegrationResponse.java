package io.github.fatec.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.fatec.entity.enumerable.StatusCampanha;

import java.math.BigDecimal;
import java.time.LocalDate;

// O que a Doação precisa da resposta do GET /campanha/{id}. Os outros campos são ignorados.
@JsonIgnoreProperties(ignoreUnknown = true)
public record CampanhaIntegrationResponse(
        String id,
        String titulo,
        BigDecimal meta,
        BigDecimal valorArrecadado,
        LocalDate dataLimite,
        StatusCampanha status
) {
}
