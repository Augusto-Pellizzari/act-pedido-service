package com.actdigital.lojaonlinepedidobe.interfaceadapter.mapper;

import com.actdigital.lojaonlinepedidobe.domain.model.StatusPedido;

public class PagamentoStatusMapper {

    public static StatusPedido toStatusPedido(String statusPagamento) {
        return switch (statusPagamento.toUpperCase()) {
            case "CONFIRMADO" -> StatusPedido.PAGO;
            case "RECUSADO" -> StatusPedido.RECUSADO;
            default -> throw new IllegalArgumentException("Status de pagamento inválido: " + statusPagamento);
        };
    }
}
