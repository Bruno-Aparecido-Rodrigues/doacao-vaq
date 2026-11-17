package io.github.fatec.event;

import io.github.fatec.event.adapter.CampanhaEventoAdapter;
import io.github.fatec.event.dto.CampanhaEvento;
import io.github.fatec.event.retry.RetryMensagem;
import io.github.fatec.service.CampanhaLocalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Escuta a fila doacao.campanha-eventos e atualiza a cópia local da vaquinha.
// Se falhar, o RetryMensagem decide: nova tentativa (dead-letter) ou parking-lot.
@Component
public class CampanhaEventoEscutador {

    private static final Logger log = LoggerFactory.getLogger(CampanhaEventoEscutador.class);

    private final CampanhaLocalService service;
    private final RetryMensagem retry;
    private final String queue;
    private final String parkingLot;

    public CampanhaEventoEscutador(CampanhaLocalService service,
                                   RetryMensagem retry,
                                   @Value("${event.campanha.queue}") String queue,
                                   @Value("${event.campanha.parking-lot}") String parkingLot) {
        this.service = service;
        this.retry = retry;
        this.queue = queue;
        this.parkingLot = parkingLot;
    }

    @RabbitListener(queues = "${event.campanha.queue}")
    public void receber(CampanhaEvento evento, Message message) {
        try {
            service.sincronizar(CampanhaEventoAdapter.cast(evento));
            log.info("Cópia local sincronizada: campanha {} ({}, retry {})",
                    evento.campanhaId(), evento.acao(), retry.retries(message, queue));
        } catch (Exception e) {
            log.error("Falha ao sincronizar campanha {}: {}", evento.campanhaId(), e.getMessage());
            retry.tratarFalha(message, queue, parkingLot, e);
        }
    }
}