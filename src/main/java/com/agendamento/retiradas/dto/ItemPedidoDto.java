package com.agendamento.retiradas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoDto {
    private Long id;
    
    @NotBlank(message = "Código é obrigatório")
    private String codigo;
    
    @NotBlank(message = "Descrição é obrigatória")
    private String descricao;
    
    @NotNull(message = "Quantidade é obrigatória")
    @Min(value = 1, message = "Quantidade deve ser maior que zero")
    private Integer quantidade;
    
    @NotBlank(message = "Unidade é obrigatória")
    private String unidade;
    
    private String ncm;
    
    private Double precoUnitario;
    
    private Double valorTotal;
    
    @NotNull(message = "ID do pedido é obrigatório")
    private Long pedidoId;
}
