package com.actdigital.lojaonlinepedidobe.application.impl;

import com.actdigital.lojaonlinepedidobe.domain.model.Pedido;
import com.actdigital.lojaonlinepedidobe.domain.model.StatusPedido;
import com.actdigital.lojaonlinepedidobe.infrastructure.event.PedidoCriadoEvent;
import com.actdigital.lojaonlinepedidobe.infrastructure.exception.CustomException;
import com.actdigital.lojaonlinepedidobe.infrastructure.exception.ErrorCode;
import com.actdigital.lojaonlinepedidobe.infrastructure.publisher.PedidoPublisher;
import com.actdigital.lojaonlinepedidobe.ports.in.PedidoService;
import com.actdigital.lojaonlinepedidobe.ports.out.PedidoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoPublisher pedidoPublisher;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, PedidoPublisher pedidoPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoPublisher = pedidoPublisher;
    }

    @Override
    public Pedido criarPedido(String cliente) {
        Pedido pedido;
        try {
            pedido = new Pedido();
            pedido.setCliente(cliente);
            pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
            pedido.setDataCriacao(LocalDateTime.now());
            pedidoRepository.salvar(pedido);
            log.debug("Pedido criado com ID={} e status={}", pedido.getId(), pedido.getStatus());
        } catch (CustomException.RepositoryException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Erro inesperado ao criar pedido para cliente={}", cliente, ex);
            throw new CustomException.ServiceException(
                    ErrorCode.ORDER_CREATION_FAILED,
                    "Não foi possível criar o pedido"
            );
        }

        try {
            OffsetDateTime agora = pedido.getDataCriacao().atOffset(ZoneOffset.UTC);
            var event = new PedidoCriadoEvent(
                    pedido.getId(),
                    pedido.getCliente(),
                    pedido.getStatus().name(),
                    agora
            );
            pedidoPublisher.publicar(event);
        } catch (AmqpException ex) {
            log.error("Erro ao publicar evento de criação de pedido id={}", pedido.getId(), ex);
            throw new CustomException.BrokerException(
                    ErrorCode.BROKER_PUBLISH_ERROR,
                    "Falha ao notificar broker sobre novo pedido"
            );
        }

        return pedido;
    }

    @Override
    public List<Pedido> listarTodos() {
        try {
            return pedidoRepository.listar();
        } catch (CustomException.RepositoryException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Erro inesperado ao listar pedidos", ex);
            throw new CustomException.ServiceException(
                    ErrorCode.ORDER_UPDATE_FAILED,
                    "Não foi possível listar pedidos"
            );
        }
    }

    @Override
    public Pedido atualizarStatus(Long id, StatusPedido status) {
        Pedido p;
        try {
            p = pedidoRepository.buscarPorId(id)
                    .orElseThrow(() -> new CustomException.NotFoundException(
                            ErrorCode.ORDER_NOT_FOUND,
                            "Pedido não encontrado: " + id
                    ));
        } catch (CustomException.NotFoundException ex) {
            throw ex;
        } catch (CustomException.RepositoryException ex) {
            throw ex;
        }

        try {
            pedidoRepository.atualizarStatus(id, status);
        } catch (CustomException.RepositoryException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Erro inesperado ao atualizar status do pedido id={}", id, ex);
            throw new CustomException.ServiceException(
                    ErrorCode.ORDER_UPDATE_FAILED,
                    "Não foi possível atualizar o pedido"
            );
        }

        return p;
    }
}
