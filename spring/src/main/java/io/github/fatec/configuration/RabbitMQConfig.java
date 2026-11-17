package io.github.fatec.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

// Filas da Doação com retry
// A volta usa a doacao.retry (direct): a nova tentativa chega SÓ nesta fila,
// e não também na fila do Relatório, que escuta os mesmos campanha.*.
@Configuration
public class RabbitMQConfig {

    @Value("${event.exchange}")
    private String exchange;

    @Value("${event.retry.exchange}")
    private String retryExchangeNome;

    @Value("${event.retry.delay}")
    private Duration retryDelay;

    @Value("${event.campanha.queue}")
    private String campanhaQueue;

    @Value("${event.campanha.dead-letter}")
    private String campanhaDeadLetter;

    @Value("${event.campanha.parking-lot}")
    private String campanhaParkingLot;

    @Value("${event.campanha.routing-key}")
    private String campanhaRoutingKey;

    @Bean
    public TopicExchange vaquinhaExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(retryExchangeNome);
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    // ---------- Filas

    @Bean
    public Queue campanhaEventosQueue() {
        return QueueBuilder.durable(campanhaQueue)
                .deadLetterExchange(retryExchangeNome)
                .deadLetterRoutingKey(campanhaDeadLetter)
                .build();
    }

    @Bean
    public Queue campanhaEventosDeadLetter() {
        return QueueBuilder.durable(campanhaDeadLetter)
                .ttl((int) retryDelay.toMillis())
                .deadLetterExchange(retryExchangeNome)
                .deadLetterRoutingKey(campanhaQueue)
                .build();
    }

    @Bean
    public Queue campanhaEventosParkingLot() {
        return QueueBuilder.durable(campanhaParkingLot).build();
    }

    // ---------- Ligações

    @Bean
    public Binding campanhaEventosBinding() {
        return BindingBuilder.bind(campanhaEventosQueue()).to(vaquinhaExchange()).with(campanhaRoutingKey);
    }

    @Bean
    public Binding campanhaEventosRetryBinding() {
        return BindingBuilder.bind(campanhaEventosQueue()).to(retryExchange()).with(campanhaQueue);
    }

    @Bean
    public Binding campanhaEventosDeadLetterBinding() {
        return BindingBuilder.bind(campanhaEventosDeadLetter()).to(retryExchange()).with(campanhaDeadLetter);
    }

    @Bean
    public Binding campanhaEventosParkingLotBinding() {
        return BindingBuilder.bind(campanhaEventosParkingLot()).to(retryExchange()).with(campanhaParkingLot);
    }
}