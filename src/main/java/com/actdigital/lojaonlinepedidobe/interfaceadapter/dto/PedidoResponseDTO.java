package com.actdigital.lojaonlinepedidobe.interfaceadapter.dto;

import com.actdigital.lojaonlinepedidobe.domain.model.StatusPedido;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PedidoResponseDTO {
    private Long id;
    private String cliente;
    private StatusPedido status;
    private LocalDateTime dataCriacao;
}
