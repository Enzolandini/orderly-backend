package com.agendamento.retiradas.repository;

import com.agendamento.retiradas.model.Pedido;
import com.agendamento.retiradas.model.StatusPedido;
import com.agendamento.retiradas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByVendedor(User vendedor);
    List<Pedido> findByStatus(StatusPedido status);
    List<Pedido> findByVendedorAndStatus(User vendedor, StatusPedido status);
    List<Pedido> findByHorarioPropostoRetiradaBetween(LocalDateTime inicio, LocalDateTime fim);
    List<Pedido> findByStatusAndHorarioPropostoRetiradaBetween(StatusPedido status, LocalDateTime inicio, LocalDateTime fim);
}
