package io.github.fatec.event.adapter;

import io.github.fatec.entity.CampanhaLocal;
import io.github.fatec.event.dto.CampanhaEvento;

// Converte a mensagem recebida da Campanha (CampanhaEvento) na cópia local do domain (CampanhaLocal).
public class CampanhaEventoAdapter {

    private CampanhaEventoAdapter() {
    }

    public static CampanhaLocal cast(CampanhaEvento evento) {
        return new CampanhaLocal(
                evento.campanhaId(),
                evento.titulo(),
                evento.meta(),
                evento.valorArrecadado(),
                evento.dataLimite(),
                evento.status(),
                evento.ativo(),
                null   // a versao é controlada pelo banco
        );
    }
}
