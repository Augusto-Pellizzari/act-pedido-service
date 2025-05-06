package com.actdigital.lojaonlinepedidobe.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Pedido {
    private Long id;
    private String cliente;
    private StatusPedido status;
    private LocalDateTime dataCriacao;

}