package com.actdigital.lojaonlinepedidobe.infrastructure.consumer;

import com.actdigital.lojaonlinepedidobe.domain.model.StatusPedido;
import com.actdigital.lojaonlinepedidobe.infrastructure.config.RabbitMQConfig;
import com.actdigital.lojaonlinepedidobe.infrastructure.event.PagamentoConfirmadoEvent;
import com.actdigital.lojaonlinepedidobe.ports.in.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PagamentoConsumer {

    private final PedidoService pedidoService;

    @RabbitListener(
            queues = RabbitMQConfig.PAGAMENTO_CONFIRMADO_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void receberPagamentoConfirmado(PagamentoConfirmadoEvent event) {
        StatusPedido novoStatus;
        if ("CONFIRMADO".equalsIgnoreCase(event.getStatus())) {
            novoStatus = StatusPedido.PAGO;
        } else if ("RECUSADO".equalsIgnoreCase(event.getStatus())) {
            novoStatus = StatusPedido.RECUSADO;
        } else {
            log.warn("Status de pagamento desconhecido='{}', marcando como RECUSADO", event.getStatus());
            novoStatus = StatusPedido.RECUSADO;
        }

        pedidoService.atualizarStatus(event.getPedidoId(), novoStatus);
        log.info("Pedido {} atualizado para {}", event.getPedidoId(), novoStatus);
    }
}