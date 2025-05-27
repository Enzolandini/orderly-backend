package com.agendamento.retiradas.controller;

import com.agendamento.retiradas.dto.PedidoDto;
import com.agendamento.retiradas.model.Pedido;
import com.agendamento.retiradas.model.StatusPedido;
import com.agendamento.retiradas.model.User;
import com.agendamento.retiradas.service.PedidoService;
import com.agendamento.retiradas.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    private final PedidoService pedidoService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<Pedido>> getAllPedidos() {
        return ResponseEntity.ok(pedidoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> getPedidoById(@PathVariable Long id) {
        return pedidoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Pedido>> getPedidosByStatus(@PathVariable StatusPedido status) {
        return ResponseEntity.ok(pedidoService.findByStatus(status));
    }

    @GetMapping("/vendedor")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<List<Pedido>> getPedidosByVendedor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User vendedor = (User) auth.getPrincipal();
        return ResponseEntity.ok(pedidoService.findByVendedor(vendedor));
    }

    @PostMapping
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<?> createPedido(
            @RequestPart("pedido") @Valid PedidoDto pedidoDto,
            @RequestPart("pdf") MultipartFile pdfFile) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User vendedor = (User) auth.getPrincipal();
            pedidoDto.setVendedorId(vendedor.getId());
            
            Pedido pedido = pedidoService.processarNovoPedido(pedidoDto, pdfFile);
            return ResponseEntity.ok(pedido);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erro ao processar o arquivo PDF: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao criar pedido: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/aprovar")
    @PreAuthorize("hasRole('EXPEDICAO')")
    public ResponseEntity<?> aprovarHorario(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User expedicao = (User) auth.getPrincipal();
            
            Pedido pedido = pedidoService.aprovarHorario(id, expedicao.getId());
            return ResponseEntity.ok(pedido);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao aprovar horário: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/reagendar")
    @PreAuthorize("hasRole('EXPEDICAO')")
    public ResponseEntity<?> sugerirNovoHorario(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime novoHorario) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User expedicao = (User) auth.getPrincipal();
            
            Pedido pedido = pedidoService.sugerirNovoHorario(id, expedicao.getId(), novoHorario);
            return ResponseEntity.ok(pedido);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao sugerir novo horário: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VENDEDOR')")
    public ResponseEntity<?> deletePedido(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User vendedor = (User) auth.getPrincipal();
            
            // Verificar se o pedido pertence ao vendedor
            Pedido pedido = pedidoService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
            
            if (!pedido.getVendedor().getId().equals(vendedor.getId())) {
                return ResponseEntity.badRequest().body("Você não tem permissão para excluir este pedido");
            }
            
            pedidoService.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao excluir pedido: " + e.getMessage());
        }
    }
}
