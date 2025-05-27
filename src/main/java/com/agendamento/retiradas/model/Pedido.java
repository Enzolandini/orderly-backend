package com.agendamento.retiradas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeCliente;

    @Column(nullable = false)
    private String numeroPedido;

    @Column(nullable = false)
    private LocalDateTime dataRegistro;

    @Column(nullable = false)
    private LocalDateTime horarioPropostoRetirada;

    private LocalDateTime horarioAprovadoRetirada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @Column(length = 1000)
    private String observacoes;

    @Column(nullable = false)
    private String caminhoArquivoPdf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private User vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expedicao_id")
    private User expedicao;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    // Método auxiliar para adicionar itens
    public void adicionarItem(ItemPedido item) {
        itens.add(item);
        item.setPedido(this);
    }

    // Método auxiliar para remover itens
    public void removerItem(ItemPedido item) {
        itens.remove(item);
        item.setPedido(null);
    }
}
