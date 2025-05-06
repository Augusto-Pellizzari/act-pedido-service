package com.actdigital.lojaonlinepedidobe.ports.in;

import com.actdigital.lojaonlinepedidobe.domain.model.Pedido;
import com.actdigital.lojaonlinepedidobe.domain.model.StatusPedido;

import java.util.List;

public interface PedidoService {

    Pedido criarPedido(String cliente);
    List<Pedido> listarTodos();
    Pedido atualizarStatus(Long id, StatusPedido status);
}
