package io.github.fatec.integration.adapter;

import io.github.fatec.entity.CampanhaLocal;
import io.github.fatec.integration.dto.CampanhaIntegrationResponse;

// Converte a resposta da Campanha na cópia local do domain.
public class CampanhaIntegrationImplAdapter {

    private CampanhaIntegrationImplAdapter() {
    }

    public static CampanhaLocal cast(CampanhaIntegrationResponse response) {
        return new CampanhaLocal(
                response.id(),
                response.titulo(),
                response.meta(),
                response.valorArrecadado(),
                response.dataLimite(),
                response.status(),
                true,   // o GET /campanha/{id} só devolve vaquinhas ativas
                null);  // a versao é controlada pelo banco
    }
}
