package com.actdigital.lojaonlinepedidobe.interfaceadapter.dto;

import com.actdigital.lojaonlinepedidobe.domain.model.StatusPedido;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PedidoResponseDTO {

    @Schema(description = "ID do pedido", example = "1")
    private Long id;

    @Schema(description = "Cliente que fez o pedido", example = "José da Silva")
    private String cliente;

    @Schema(description = "Status atual do pedido", example = "AGUARDANDO_PAGAMENTO")
    private StatusPedido status;

    @Schema(description = "Data e hora de criação", example = "2025-05-07T10:15:30")
    private LocalDateTime dataCriacao;
}
