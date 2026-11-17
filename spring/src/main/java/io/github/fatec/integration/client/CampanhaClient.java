package io.github.fatec.integration.client;

import io.github.fatec.integration.dto.CampanhaIntegrationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Cliente HTTP da Campanha
// Chama direto a porta da Campanha, sem passar pelo gateway: é conversa entre serviços.
@FeignClient(name = "campanha", url = "${campanha.url}")
public interface CampanhaClient {

    @GetMapping("/campanha/{id}")
    CampanhaIntegrationResponse buscar(@PathVariable("id") String id);
}
