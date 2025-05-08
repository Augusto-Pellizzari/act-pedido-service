package com.actdigital.lojaonlinepedidobe.interfaceadapter.controller;

import com.actdigital.lojaonlinepedidobe.domain.model.Pedido;
import com.actdigital.lojaonlinepedidobe.domain.model.StatusPedido;
import com.actdigital.lojaonlinepedidobe.interfaceadapter.dto.PedidoRequestDTO;
import com.actdigital.lojaonlinepedidobe.interfaceadapter.dto.PedidoResponseDTO;
import com.actdigital.lojaonlinepedidobe.ports.in.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Pedidos", description = "Chamadas sobre pedidos")
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @Operation(summary = "Cria um novo pedido")
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criar(@RequestBody PedidoRequestDTO dto) {
        Pedido pedido = pedidoService.criarPedido(dto.getCliente());
        return ResponseEntity.ok(toDTO(pedido));
    }

    @Operation(summary = "Lista todos os pedidos")
    @GetMapping
    public List<PedidoResponseDTO> listar() {
        return pedidoService.listarTodos().stream().map(this::toDTO).toList();
    }

    @Operation(summary = "Atualiza status de um pedido")
    @PutMapping("/{id}/status")
    public ResponseEntity<PedidoResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestParam StatusPedido status
    ) {
        Pedido p = pedidoService.atualizarStatus(id, status);
        return ResponseEntity.ok(toDTO(p));
    }

    private PedidoResponseDTO toDTO(Pedido p) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(p.getId());
        dto.setCliente(p.getCliente());
        dto.setStatus(p.getStatus());
        dto.setDataCriacao(p.getDataCriacao());
        return dto;
    }
}
