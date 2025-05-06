package com.actdigital.lojaonlinepedidobe.infrastructure.publisher;

import com.actdigital.lojaonlinepedidobe.infrastructure.config.RabbitMQConfig;
import com.actdigital.lojaonlinepedidobe.infrastructure.event.PedidoCriadoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class PedidoPublisher {
    private final RabbitTemplate rabbitTemplate;

    public PedidoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitTemplate.setConfirmCallback((corr, ack, cause) -> {
            String corrId = corr != null ? corr.getId() : "null";
            if (!ack) log.error("NACK do broker para corrId={} cause={}", corrId, cause);
            else      log.debug("ACK  do broker para corrId={}", corrId);
        });
    }

    public void publicar(PedidoCriadoEvent evt) {
        String corrId = UUID.randomUUID().toString();
        CorrelationData cd = new CorrelationData(corrId);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PEDIDO_CRIADO_EXCHANGE,
                RabbitMQConfig.PEDIDO_CRIADO_ROUTING_KEY,
                evt,
                message -> {
                    message.getMessageProperties().setMessageId(corrId);
                    message.getMessageProperties().setCorrelationId(corrId);
                    return message;
                },
                cd
        );

        log.debug("Publicado PedidoCriadoEvent pedidoId={} corrId={}", evt.getId(), corrId);
    }
}