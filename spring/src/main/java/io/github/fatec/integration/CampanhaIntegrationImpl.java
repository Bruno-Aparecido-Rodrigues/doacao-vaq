package io.github.fatec.integration;

import feign.FeignException;
import io.github.fatec.entity.CampanhaLocal;
import io.github.fatec.integration.adapter.CampanhaIntegrationImplAdapter;
import io.github.fatec.integration.client.CampanhaClient;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Implementa o contrato CampanhaIntegration do domain
// @Retry repete a chamada quando a Campanha está fora do ar (configuração "campanha" no application.yml);
// se mesmo assim não der, o fallback devolve "não encontrada" em vez de estourar erro.
@Component
public class CampanhaIntegrationImpl implements CampanhaIntegration {

    private static final Logger log = LoggerFactory.getLogger(CampanhaIntegrationImpl.class);

    private final CampanhaClient client;

    public CampanhaIntegrationImpl(CampanhaClient client) {
        this.client = client;
    }

    @Override
    @Retry(name = "campanha", fallbackMethod = "campanhaNaoEncontrada")
    public Optional<CampanhaLocal> buscar(String campanhaId) {
        log.info("Cópia local da campanha {} não existe; buscando na Campanha", campanhaId);
        return Optional.of(CampanhaIntegrationImplAdapter.cast(client.buscar(campanhaId)));
    }

    // Fallback do 404: a vaquinha não existe (ou foi excluída). Não houve retry.
    private Optional<CampanhaLocal> campanhaNaoEncontrada(String campanhaId, FeignException.NotFound ex) {
        log.warn("Campanha {} não existe no serviço de Campanha", campanhaId);
        return Optional.empty();
    }

    // Fallback de qualquer outro erro: a Campanha não respondeu nem depois das tentativas.
    private Optional<CampanhaLocal> campanhaNaoEncontrada(String campanhaId, Exception ex) {
        log.error("Campanha indisponível após as tentativas ao buscar {}: {}", campanhaId, ex.getMessage());
        return Optional.empty();
    }
}
