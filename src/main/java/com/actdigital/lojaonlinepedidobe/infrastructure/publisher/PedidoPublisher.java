package com.actdigital.lojaonlinepedidobe.infrastructure.publisher;

import com.actdigital.lojaonlinepedidobe.infrastructure.config.RabbitMQConfig;
import com.actdigital.lojaonlinepedidobe.infrastructure.event.PedidoCriadoEvent;
import com.actdigital.lojaonlinepedidobe.infrastructure.exception.CustomException;
import com.actdigital.lojaonlinepedidobe.infrastructure.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
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

    public void publicar(Object evt) {
        String corrId = UUID.randomUUID().toString();
        CorrelationData cd = new CorrelationData(corrId);
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PEDIDO_CRIADO_EXCHANGE,
                    RabbitMQConfig.PEDIDO_CRIADO_ROUTING_KEY,
                    evt,
                    msg -> {
                        msg.getMessageProperties().setMessageId(corrId);
                        msg.getMessageProperties().setCorrelationId(corrId);
                        return msg;
                    },
                    cd
            );
            log.debug("Publicado evento corrId={}", corrId);
        } catch (AmqpException ex) {
            log.error("Erro ao enviar mensagem ao broker, corrId={}", corrId, ex);
            throw new CustomException.BrokerException(
                    ErrorCode.BROKER_PUBLISH_ERROR,
                    "Falha ao enviar mensagem ao broker"
            );
        }
    }
}