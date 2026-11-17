package io.github.fatec.event;

import io.github.fatec.entity.Doacao;
import io.github.fatec.entity.OperacaoDoacao;
import io.github.fatec.event.dto.DoacaoRealizadaEvento;
import io.github.fatec.event.dto.OperacaoEvento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

// Publica doacao.realizada (para a Campanha) e operacao.doacao (para o Relatório).
// Converte os objetos do domain nas mensagens JSON (event/dto) e envia para a exchange.
@Component
public class DoacaoPublicadorImpl implements DoacaoPublicador {

    private static final Logger log = LoggerFactory.getLogger(DoacaoPublicadorImpl.class);
    private static final String TIPO_OPERACAO = "DOACAO";

    private final RabbitTemplate rabbitTemplate;

    @Value("${event.exchange}")
    private String exchange;

    @Value("${event.doacao-realizada.routing-key}")
    private String doacaoRealizadaRoutingKey;

    @Value("${event.operacao.routing-key}")
    private String operacaoRoutingKey;

    public DoacaoPublicadorImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarDoacaoRealizada(Doacao doacao) {
        DoacaoRealizadaEvento evento = new DoacaoRealizadaEvento(
                doacao.operacaoId(),
                doacao.id(),
                doacao.campanhaId(),
                doacao.valor(),
                doacao.doadorId(),
                doacao.doadorNome(),
                doacao.data());
        try {
            rabbitTemplate.convertAndSend(exchange, doacaoRealizadaRoutingKey, evento);
            log.info("Publicado {}: doação {} de {} na campanha {}",
                    doacaoRealizadaRoutingKey, evento.doacaoId(), evento.valor(), evento.campanhaId());
        } catch (Exception e) {
            log.error("Falha ao publicar doação realizada {}: {}", evento.doacaoId(), e.getMessage());
        }
    }

    @Override
    public void publicarOperacao(OperacaoDoacao operacao) {
        OperacaoEvento evento = new OperacaoEvento(
                operacao.operacaoId(),
                TIPO_OPERACAO,
                operacao.status(),
                operacao.campanhaId(),
                operacao.campanhaTitulo(),
                operacao.doadorNome(),
                operacao.valor(),
                operacao.tentativas(),
                operacao.motivo(),
                Instant.now());
        try {
            rabbitTemplate.convertAndSend(exchange, operacaoRoutingKey, evento);
            log.info("Operação {} -> {} (tentativa {})",
                    evento.operacaoId(), evento.status(), evento.tentativas());
        } catch (Exception e) {
            log.error("Falha ao publicar operação {}: {}", evento.operacaoId(), e.getMessage());
        }
    }
}
