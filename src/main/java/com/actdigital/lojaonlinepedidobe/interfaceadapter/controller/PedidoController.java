package com.actdigital.lojaonlinepedidobe.interfaceadapter.controller;

import com.actdigital.lojaonlinepedidobe.ports.in.PedidoService;
import com.actdigital.lojaonlinepedidobe.domain.model.Pedido;
import com.actdigital.lojaonlinepedidobe.domain.model.StatusPedido;
import com.actdigital.lojaonlinepedidobe.interfaceadapter.dto.PedidoRequestDTO;
import com.actdigital.lojaonlinepedidobe.interfaceadapter.dto.PedidoResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> criar(@RequestBody PedidoRequestDTO dto) {
        Pedido pedido = pedidoService.criarPedido(dto.getCliente());
        return ResponseEntity.ok(toDTO(pedido));
    }

    @GetMapping
    public List<PedidoResponseDTO> listar() {
        return pedidoService.listarTodos().stream().map(this::toDTO).toList();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PedidoResponseDTO> atualizar(@PathVariable Long id, @RequestParam StatusPedido status) {
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
