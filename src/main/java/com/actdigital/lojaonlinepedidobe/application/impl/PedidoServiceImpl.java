package com.actdigital.lojaonlinepedidobe.application.impl;

import com.actdigital.lojaonlinepedidobe.domain.model.Pedido;
import com.actdigital.lojaonlinepedidobe.domain.model.StatusPedido;
import com.actdigital.lojaonlinepedidobe.infrastructure.event.PedidoCriadoEvent;
import com.actdigital.lojaonlinepedidobe.infrastructure.publisher.PedidoPublisher;
import com.actdigital.lojaonlinepedidobe.ports.in.PedidoService;
import com.actdigital.lojaonlinepedidobe.ports.out.PedidoRepository;
import lombok.extern.slf4j.Slf4j;
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
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setDataCriacao(LocalDateTime.now());

        pedidoRepository.salvar(pedido);

        log.info("Pedido criado com ID={} e status={}", pedido.getId(), pedido.getStatus());

        OffsetDateTime agora = pedido.getDataCriacao().atOffset(ZoneOffset.UTC);
        PedidoCriadoEvent event = new PedidoCriadoEvent(
                pedido.getId(),
                pedido.getCliente(),
                pedido.getStatus().name(),
                agora
        );
        pedidoPublisher.publicar(event);

        return pedido;
    }

    @Override
    public List<Pedido> listarTodos() {
        return pedidoRepository.listar();
    }

    @Override
    public Pedido atualizarStatus(Long id, StatusPedido status) {
        pedidoRepository.atualizarStatus(id, status);
        return pedidoRepository.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
    }
}
