package com.agendamento.retiradas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDto {
    private Long id;
    
    @NotBlank(message = "Nome do cliente é obrigatório")
    private String nomeCliente;
    
    @NotBlank(message = "Número do pedido é obrigatório")
    private String numeroPedido;
    
    @NotNull(message = "Horário proposto para retirada é obrigatório")
    private LocalDateTime horarioPropostoRetirada;
    
    private LocalDateTime horarioAprovadoRetirada;
    
    private String observacoes;
    
    @NotNull(message = "ID do vendedor é obrigatório")
    private Long vendedorId;
    
    private Long expedicaoId;
}
