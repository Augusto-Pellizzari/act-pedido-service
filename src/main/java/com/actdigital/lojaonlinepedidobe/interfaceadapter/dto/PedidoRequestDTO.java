package com.actdigital.lojaonlinepedidobe.interfaceadapter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PedidoRequestDTO {

    @Schema(
            description = "Nome do cliente que está fazendo o pedido",
            example     = "José da Silva",
            required    = true
    )
    private String cliente;
}
