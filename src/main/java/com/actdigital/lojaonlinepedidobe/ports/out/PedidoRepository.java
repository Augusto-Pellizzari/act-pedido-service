package com.actdigital.lojaonlinepedidobe.ports.out;

import com.actdigital.lojaonlinepedidobe.domain.model.Pedido;
import com.actdigital.lojaonlinepedidobe.domain.model.StatusPedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository {

    void salvar(Pedido pedido);
    List<Pedido> listar();
    Optional<Pedido> buscarPorId(Long id);
    void atualizarStatus(Long id, StatusPedido status);
}
