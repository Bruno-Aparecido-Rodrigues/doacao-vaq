package io.github.fatec.service;

import io.github.fatec.entity.CampanhaLocal;
import io.github.fatec.repository.CampanhaLocalRepository;

// Mantém a cópia local das vaquinhas atualizada com o que a Campanha publicou.
// O escutador (módulo spring) converte a mensagem em CampanhaLocal e chama este service.
public class CampanhaLocalService {

    private final CampanhaLocalRepository repository;

    public CampanhaLocalService(CampanhaLocalRepository repository) {
        this.repository = repository;
    }

    public void sincronizar(CampanhaLocal campanha) {
        if (campanha.id() == null || campanha.meta() == null || campanha.status() == null) {
            throw new IllegalArgumentException("Evento de campanha incompleto: " + campanha);
        }
        repository.sincronizar(campanha);
    }
}
